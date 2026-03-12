package io.github.pengxianggui.crud.export;

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
import org.springframework.util.ClassUtils;

import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pengxg
 * @date 2025/5/3 11:43
 */
@Slf4j
public class ExcelExportManager {
    // 使用jackson进行序列化，以便业务系统可以使用自定义的序列化逻辑干预excel导出
    private final ObjectMapper objectMapper;

    public ExcelExportManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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

        List<List<Object>> dataList = data.stream().map(obj -> {
            Map<String, Object> mappedObj = objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {
            });
            List<Object> rowData = new ArrayList<>();
            for (String col : cols) {
                rowData.add(mappedObj.get(col));
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
    private ColumnHandler getColumnHandler(Map<String, Object> columnConfig) {
        String columnType = (String) columnConfig.get("tableColumnComponentName");
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
}
