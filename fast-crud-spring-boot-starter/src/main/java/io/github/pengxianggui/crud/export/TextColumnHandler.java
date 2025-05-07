package io.github.pengxianggui.crud.export;

import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;

import java.util.Map;

/**
 * @author pengxg
 * @date 2025/5/3 10:45
 */
public class TextColumnHandler extends ColumnHandler {

    public TextColumnHandler(String component, Map<String, Object> columnConfig) {
        super(component, columnConfig);
    }

    public void handleData(CellWriteHandlerContext context, Object value) {
        context.getCell().setCellValue(value == null ? "" : value.toString());
    }
}
