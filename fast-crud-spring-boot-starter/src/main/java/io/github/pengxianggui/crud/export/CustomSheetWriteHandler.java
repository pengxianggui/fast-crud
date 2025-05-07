package io.github.pengxianggui.crud.export;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

import java.util.List;
import java.util.Map;

/**
 * @author pengxg
 * @date 2025/5/3 11:19
 */
@Slf4j
public class CustomSheetWriteHandler implements SheetWriteHandler {
    private final List<Integer> widths;
    private final Map<Integer, ColumnHandler> handlerMapping;

    public CustomSheetWriteHandler(List<Integer> widths, Map<Integer, ColumnHandler> handlerMapping) {
        this.widths = widths;
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        Sheet sheet = writeSheetHolder.getSheet();

        // 设置列宽
        for (int i = 0; i < widths.size(); i++) {
            sheet.setColumnWidth(i, widths.get(i) * 256);
        }

        // 设置列样式
        Workbook workbook = writeWorkbookHolder.getWorkbook();
        Row headerRow = sheet.getRow(0); // 获取标题行

        for (int i = 0; i < handlerMapping.size(); i++) {
            ColumnHandler handler = handlerMapping.get(i);
            CellStyle style = workbook.createCellStyle(); // 创建样式对象
            handler.handleHead(writeSheetHolder, style, i); // 让处理器处理表头样式
            // 应用样式到标题单元格
            if (headerRow != null) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    cell.setCellStyle(style);
                }
            }
        }
    }

}
