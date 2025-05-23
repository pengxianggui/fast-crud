package io.github.pengxianggui.crud.export;

import cn.hutool.core.util.ReflectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.converters.localdate.LocalDateDateConverter;
import com.alibaba.excel.converters.localdatetime.LocalDateTimeDateConverter;
import com.alibaba.excel.converters.localdatetime.LocalDateTimeStringConverter;
import com.alibaba.excel.write.metadata.WriteSheet;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pengxg
 * @date 2025/5/3 11:43
 */
@Slf4j
public class ExcelExportManager {

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
            Map<String, Object> column = columns.get(i);
            String col = (String) column.get("col");
            String label = (String) column.get("label");
            cols.add(col);
            head.add(Collections.singletonList(label));
            ColumnHandler handler = getColumnHandler(column);
            widths.add(handler.getColumnWidth());
            handlerMapping.put(i, handler);
        }

        List<List<Object>> dataList = data.stream().map(obj -> {
            List<Object> rowData = new ArrayList<>();
            for (String col : cols) {
                rowData.add(ReflectUtil.getFieldValue(obj, col));
            }
            return rowData;
        }).collect(Collectors.toList());

        ExcelWriter excelWriter = EasyExcel.write(outputStream)
                .head(head)
                .includeColumnFieldNames(cols)
                .registerWriteHandler(new CustomSheetWriteHandler(widths, handlerMapping))
                .registerWriteHandler(new CustomCellWriteHandler(handlerMapping))
                .registerConverter(new LocalDateTimeStringConverter())
                .registerConverter(new LocalDateTimeDateConverter())
                .registerConverter(new LocalDateDateConverter())
                .registerConverter(new LocalTimeConverter())
                .build();
        WriteSheet sheet = EasyExcel.writerSheet("Sheet1").build();
        excelWriter.write(dataList, sheet);
        excelWriter.finish();
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
            case "fast-table-column-date-picker":
                return new DateColumnHandler(columnType, columnConfig);
//            case "fast-table-column-time-picker":
//                return new TimeColumnHandler(columnType, columnConfig);
            case "fast-table-column-img":
                return new ImageColumnHandler(columnType, columnConfig);
            case "fast-table-column-file":
                return new FileColumnHandler(columnType, columnConfig);
            case "fast-table-column-number":
                return new NumberColumnHandler(columnType, columnConfig);
            case "fast-table-column-select":
            case "fast-table-column-switch":
                return new SelectColumnHandler(columnType, columnConfig);
            case "fast-table-column":
            case "fast-table-column-input":
            case "fast-table-column-textarea":
            default:
                return new TextColumnHandler(columnType, columnConfig);
        }
    }
}
