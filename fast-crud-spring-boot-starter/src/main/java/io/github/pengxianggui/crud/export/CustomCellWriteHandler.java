package io.github.pengxianggui.crud.export;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;
import java.util.Map;

/**
 * @author
 * @date 2025/5/6 08:43
 */
public class CustomCellWriteHandler implements CellWriteHandler {
    private final Map<Integer, ColumnHandler> handlerMapping;

    public CustomCellWriteHandler(Map<Integer, ColumnHandler> handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void beforeCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Head head, Integer columnIndex, Integer relativeRowIndex, Boolean isHead) {
        CellWriteHandler.super.beforeCellCreate(writeSheetHolder, writeTableHolder, row, head, columnIndex, relativeRowIndex, isHead);
    }

    @Override
    public void afterCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        CellWriteHandler.super.afterCellCreate(writeSheetHolder, writeTableHolder, cell, head, relativeRowIndex, isHead);
    }

    @Override
    public void afterCellDispose(CellWriteHandlerContext context) {
        Boolean isHead = context.getHead();
        if (isHead) {
            return; // 只处理数据行, 表头不处理
        }

        Cell cell = context.getCell();
        int columnIndex = cell.getColumnIndex();
        if (columnIndex < handlerMapping.size()) {
            ColumnHandler handler = handlerMapping.get(columnIndex);
            List<WriteCellData<?>> cellDataList = context.getCellDataList();
            if (handler != null) {
                Object value = getCellValue(cellDataList);
                handler.handleData(context, value);
            }
        }
    }

    private Object getCellValue(List<WriteCellData<?>> cellDataList) {
        if (CollectionUtil.isEmpty(cellDataList)) {
            return null;
        }
        WriteCellData cellData = cellDataList.get(0);
        switch (cellData.getType()) {
            case STRING:
            case RICH_TEXT_STRING:
                return cellData.getStringValue();
            case NUMBER:
                return cellData.getNumberValue();
            case BOOLEAN:
                return cellData.getBooleanValue();
            case DATE:
                return cellData.getDateValue();
            case ERROR:
            case EMPTY:
            default:
                return cellData.getData();
        }
    }
}
