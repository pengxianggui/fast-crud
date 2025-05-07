package io.github.pengxianggui.crud.export;

import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Map;

/**
 * @author pengxg
 * @date 2025/5/3 10:56
 */
public class NumberColumnHandler extends ColumnHandler {

    public NumberColumnHandler(String component, Map<String, Object> columnConfig) {
        super(component, columnConfig);
    }

    @Override
    public void handleData(CellWriteHandlerContext context, Object value) {
        if (value != null) {
            Cell cell = context.getCell();
            try {
                cell.setCellValue(Double.parseDouble(value.toString()));
            } catch (Exception e) {
                cell.setCellValue(value.toString());
            }
        }
    }
}
