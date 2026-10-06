package io.github.pengxianggui.crud.export;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pengxg
 * @date 2025/5/3 11:19
 */
@Slf4j
public class CustomSheetWriteHandler implements SheetWriteHandler {
    /**
     * 下拉选项的隐藏sheet名。当选项文案含逗号或超长时, 下拉改为引用此sheet的区间
     */
    private static final String OPTION_SHEET_NAME = "__fc_options__";
    /**
     * excel内联下拉列表(逗号分隔)的最大长度
     */
    private static final int MAX_INLINE_OPTION_LENGTH = 255;

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

        // 设置下拉校验
        writeOptionValidation(workbook, sheet, collectOptions());
    }

    private Map<Integer, List<String>> collectOptions() {
        Map<Integer, List<String>> optionColumns = new LinkedHashMap<>();
        for (Map.Entry<Integer, ColumnHandler> entry : handlerMapping.entrySet()) {
            ColumnHandler handler = entry.getValue();
            if (handler == null) {
                continue;
            }
            List<String> labels = handler.getOptionLabels();
            if (CollectionUtil.isNotEmpty(labels)) {
                optionColumns.put(entry.getKey(), labels);
            }
        }
        return optionColumns;
    }

    /**
     * 写入下拉数据校验。
     * <p>
     * excel的内联列表(explicit list)以逗号分隔, 因此选项文案中若含英文逗号, 会导致一个选项被拆成两个;
     * 同时内联列表还有255字符的长度上限。出现以上情况时, 降级为引用隐藏sheet的区间校验(区间校验无这两个限制)。
     * 若整个文件都不需要降级, 则维持内联列表, 避免产生额外的隐藏sheet。
     *
     * @param workbook     工作簿
     * @param sheet        数据sheet
     * @param optionColumns key-列号, value-该列的选项文案
     */
    static void writeOptionValidation(Workbook workbook, Sheet sheet, Map<Integer, List<String>> optionColumns) {
        if (optionColumns == null || optionColumns.isEmpty()) {
            return;
        }
        boolean inlineSafe = optionColumns.values().stream().allMatch(CustomSheetWriteHandler::inlineSafe);
        if (inlineSafe) {
            optionColumns.forEach((columnIndex, labels) -> addInlineValidation(sheet, columnIndex, labels));
            return;
        }
        addRangeValidation(workbook, sheet, optionColumns);
    }

    /**
     * 是否可以使用内联列表(选项文案不含逗号且未超长)
     */
    private static boolean inlineSafe(List<String> labels) {
        boolean hasComma = labels.stream().anyMatch(label -> label != null && label.contains(","));
        return !hasComma && String.join(",", labels).length() <= MAX_INLINE_OPTION_LENGTH;
    }

    private static void addInlineValidation(Sheet sheet, int columnIndex, List<String> labels) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        CellRangeAddressList rangeList = new CellRangeAddressList(1, 65535, columnIndex, columnIndex);
        DataValidationConstraint constraint = helper.createExplicitListConstraint(labels.toArray(new String[0]));
        addValidation(sheet, constraint, rangeList);
    }

    /**
     * 将选项写入隐藏sheet, 并通过区间引用做下拉校验
     */
    private static void addRangeValidation(Workbook workbook, Sheet sheet, Map<Integer, List<String>> optionColumns) {
        Sheet optionSheet = null;
        boolean created = false;
        try {
            optionSheet = workbook.getSheet(OPTION_SHEET_NAME);
            if (optionSheet == null) {
                optionSheet = workbook.createSheet(OPTION_SHEET_NAME);
                created = true;
            }
            DataValidationHelper helper = sheet.getDataValidationHelper();
            int optionColumnIndex = 0;
            for (Map.Entry<Integer, List<String>> entry : optionColumns.entrySet()) {
                List<String> labels = entry.getValue();
                String columnLetter = CellReference.convertNumToColString(optionColumnIndex);
                for (int rowIndex = 0; rowIndex < labels.size(); rowIndex++) {
                    Row row = optionSheet.getRow(rowIndex);
                    if (row == null) {
                        row = optionSheet.createRow(rowIndex);
                    }
                    row.createCell(optionColumnIndex).setCellValue(labels.get(rowIndex));
                }
                String formula = String.format("'%s'!$%s$1:$%s$%d",
                        OPTION_SHEET_NAME, columnLetter, columnLetter, labels.size());
                DataValidationConstraint constraint = helper.createFormulaListConstraint(formula);
                CellRangeAddressList rangeList = new CellRangeAddressList(1, 65535, entry.getKey(), entry.getKey());
                addValidation(sheet, constraint, rangeList);
                optionColumnIndex++;
            }
            workbook.setSheetHidden(workbook.getSheetIndex(optionSheet), true);
        } catch (Exception e) {
            log.error("Fast crud export: failed to write option validation, dropdown will be skipped", e);
            if (created && optionSheet != null) {
                try { // 失败时不残留多余的sheet
                    workbook.removeSheetAt(workbook.getSheetIndex(optionSheet));
                } catch (Exception ignored) {
                    // ignore
                }
            }
        }
    }

    private static void addValidation(Sheet sheet, DataValidationConstraint constraint, CellRangeAddressList rangeList) {
        DataValidation validation = sheet.getDataValidationHelper().createValidation(constraint, rangeList);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("ERROR", "Please choose value of options");
        sheet.addValidationData(validation);
    }
}
