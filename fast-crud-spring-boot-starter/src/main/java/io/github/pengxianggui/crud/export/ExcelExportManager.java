package io.github.pengxianggui.crud.export;

import cn.hutool.core.util.ReflectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.converters.localdate.LocalDateDateConverter;
import com.alibaba.excel.converters.localdatetime.LocalDateTimeDateConverter;
import com.alibaba.excel.converters.localdatetime.LocalDateTimeStringConverter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.ClassUtils;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pengxg
 * @date 2025/5/3 11:43
 */
@Slf4j
public class ExcelExportManager {
    // Excel 单元格文本内容的最大字符数限制（来源于 OOXML 规范，POI 强制校验）
    private static final int MAX_CELL_TEXT_LENGTH = 32767;
    private static final String TRUNCATE_SUFFIX = "...";

    // 使用jackson进行序列化，以便业务系统可以使用自定义的序列化逻辑干预excel导出
    private final ObjectMapper objectMapper;
    private final List<ExcelValueConverter<?>> valueConverters;

    public ExcelExportManager(ObjectMapper objectMapper) {
        this(objectMapper, Collections.emptyList());
    }

    public ExcelExportManager(ObjectMapper objectMapper, List<ExcelValueConverter<?>> valueConverters) {
        this.objectMapper = objectMapper;
        List<ExcelValueConverter<?>> sortedConverters = new ArrayList<>();
        if (valueConverters != null) {
            sortedConverters.addAll(valueConverters);
            AnnotationAwareOrderComparator.sort(sortedConverters);
        }
        this.valueConverters = sortedConverters;
    }

    /**
     * 按配置导出数据
     *
     * @param data         要导出的数据
     * @param columns      要导出的列配置
     * @param outputStream 输出流
     */
    public void exportByConfig(List<?> data, List<Map<String, Object>> columns, OutputStream outputStream) {
        List<List<String>> head = new ArrayList<>();
        List<Integer> widths = new ArrayList<>();
        Map<Integer, ColumnHandler> handlerMapping = new HashMap<>();
        List<String> cols = new ArrayList<>();
        List<Map<String, Object>> filteredColumns = columns.stream()
                .filter(column -> Boolean.TRUE.equals(column.get("exportable"))).collect(Collectors.toList());
        for (int i = 0; i < filteredColumns.size(); i++) {
            Map<String, Object> column = filteredColumns.get(i);
            String col = (String) column.get("col");
            String label = (String) column.get("label");
            cols.add(col);
            head.add(Collections.singletonList(label));
            ColumnHandler handler = getColumnHandler(column);
            widths.add(handler.getColumnWidth());
            handlerMapping.put(i, handler);
        }

        Set<String> warnedFallbackColumns = new HashSet<>();
        List<List<Object>> dataList = data.stream().map(obj -> {
            Map<String, Object> mappedObj = objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {
            });
            List<Object> rowData = new ArrayList<>();
            for (String col : cols) {
                Object value = convertValue(obj, mappedObj.get(col), col, warnedFallbackColumns);
                rowData.add(truncateCellValue(value, col));
            }
            return rowData;
        }).collect(Collectors.toList());

        ExcelWriterBuilder writerBuilder = EasyExcel.write(outputStream)
                .head(head)
                .includeColumnFieldNames(cols)
                .registerWriteHandler(new CustomSheetWriteHandler(widths, handlerMapping))
                .registerWriteHandler(new CustomCellWriteHandler(handlerMapping))
                .registerConverter(new ArrayListConverter())
                .registerConverter(new LocalTimeConverter());
        ClassLoader classLoader = this.getClass().getClassLoader();
        if (ClassUtils.isPresent("com.alibaba.excel.converters.localdatetime.LocalDateTimeStringConverter", classLoader)) {
            writerBuilder.registerConverter(new LocalDateTimeStringConverter());
        }
        if (ClassUtils.isPresent("com.alibaba.excel.converters.localdatetime.LocalDateTimeDateConverter", classLoader)) {
            writerBuilder.registerConverter(new LocalDateTimeDateConverter());
        }
        if (ClassUtils.isPresent("com.alibaba.excel.converters.localdate.LocalDateDateConverter", classLoader)) {
            writerBuilder.registerConverter(new LocalDateDateConverter());
        }
        ExcelWriter writer = writerBuilder.build();
        WriteSheet sheet = EasyExcel.writerSheet("Sheet1").build();
        writer.write(dataList, sheet);
        writer.finish();
    }

    // TODO 大表时需要分页追加写入

    /**
     * 根据列配置获取列处理器
     *
     * @param columnConfig
     * @return
     */
    public static ColumnHandler getColumnHandler(Map<String, Object> columnConfig) {
        String columnType = (String) columnConfig.get("tableColumnComponentName");
        if (columnType == null) {
            return new TextColumnHandler("", columnConfig);
        }
        switch (columnType) {
            case "FastTableColumnDatePicker":
            case "fast-table-column-date-picker":
                return new DateColumnHandler(columnType, columnConfig);
            case "FastTableColumnImg":
            case "fast-table-column-img":
                return new ImageColumnHandler(columnType, columnConfig);
            case "FastTableColumnFile":
            case "fast-table-column-file":
                return new FileColumnHandler(columnType, columnConfig);
            case "FastTableColumnNumber":
            case "fast-table-column-number":
                return new NumberColumnHandler(columnType, columnConfig);
            case "FastTableColumnSelect":
            case "fast-table-column-select":
            case "FastTableColumnSwitch":
            case "fast-table-column-switch":
                return new SelectColumnHandler(columnType, columnConfig);
            case "FastTableColumn":
            case "fast-table-column":
            case "FastTableColumnInput":
            case "fast-table-column-input":
            case "FastTableColumnTextarea":
            case "fast-table-column-textarea":
            case "FastTableColumnTimePicker":
            case "fast-table-column-time-picker":
            default:
                return new TextColumnHandler(columnType, columnConfig);
        }
    }

    /**
     * 优先使用按字段声明类型匹配的转换器, 无匹配时保留Jackson序列化结果。
     */
    private Object convertValue(Object source, Object serializedValue, String col, Set<String> warnedFallbackColumns) {
        Field field = findField(source.getClass(), col);
        if (field == null) {
            return fallbackSerializedValue(source, serializedValue, col, null, warnedFallbackColumns);
        }
        ExcelValueConverter<?> converter = findConverter(field.getType());
        if (converter == null) {
            return fallbackSerializedValue(source, serializedValue, col, field.getType(), warnedFallbackColumns);
        }
        Object rawValue = ReflectUtil.getFieldValue(source, field);
        return converter.convertValue(rawValue);
    }

    /**
     * 无匹配转换器时保留EasyExcel可写的Jackson结果。
     * 对于Map等无法直接写入的类型降级为空串, 避免单个字段导致整个导出失败。
     */
    private Object fallbackSerializedValue(Object source, Object value, String col, Class<?> fieldType,
                                           Set<String> warnedFallbackColumns) {
        if (value == null
                || value instanceof CharSequence
                || value instanceof Character
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Date
                || value instanceof Temporal
                || value instanceof ArrayList) {
            return value;
        }
        String warningKey = source.getClass().getName() + "#" + col;
        if (warnedFallbackColumns.add(warningKey)) {
            log.error("No matching ExcelValueConverter, export cell as blank. class: {}, field: {}, fieldType: {}, serializedType: {}",
                    source.getClass().getName(), col, fieldType == null ? "unknown" : fieldType.getName(),
                    value.getClass().getName());
        }
        return "";
    }

    private ExcelValueConverter<?> findConverter(Class<?> fieldType) {
        for (ExcelValueConverter<?> converter : valueConverters) {
            if (converter.supports(fieldType)) {
                return converter;
            }
        }
        return null;
    }

    private static Field findField(Class<?> clazz, String fieldName) {
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                // 继续向父类查找
            }
        }
        return null;
    }

    /**
     * 对超出 Excel 单元格文本长度上限的值做截断处理，避免写文件时抛异常。
     *
     * @param value 单元格原始值
     * @param col   当前列名（用于日志定位）
     * @return 截断后的值，未超长时原样返回
     */
    private Object truncateCellValue(Object value, String col) {
        if (value instanceof String) {
            String text = (String) value;
            if (text.length() > MAX_CELL_TEXT_LENGTH) {
                String truncated = text.substring(0, MAX_CELL_TEXT_LENGTH - TRUNCATE_SUFFIX.length()) + TRUNCATE_SUFFIX;
                log.warn("Cell content exceeds Excel limit and has been truncated, column: {}, original length: {}, max length: {}", col, text.length(), MAX_CELL_TEXT_LENGTH);
                return truncated;
            }
        }
        return value;
    }
}
