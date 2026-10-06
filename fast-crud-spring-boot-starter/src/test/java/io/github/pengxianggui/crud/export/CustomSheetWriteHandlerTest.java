package io.github.pengxianggui.crud.export;

import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 导出下拉校验的测试: 选项文案含逗号时不能被拆成两个选项
 *
 * @author pengxg
 */
class CustomSheetWriteHandlerTest {

    @Test
    void commaLabelShouldUseHiddenSheetRange() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            sheet.createRow(0).createCell(0).setCellValue("状态");

            Map<Integer, List<String>> optionColumns = new LinkedHashMap<>();
            optionColumns.put(0, Arrays.asList("启用,中", "停用"));

            CustomSheetWriteHandler.writeOptionValidation(workbook, sheet, optionColumns);

            // 选项写入隐藏sheet, 逗号完整保留(不会被拆成两个选项)
            Sheet optionSheet = workbook.getSheet("__fc_options__");
            assertNotNull(optionSheet, "含逗号的选项应使用隐藏sheet");
            assertEquals("启用,中", optionSheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("停用", optionSheet.getRow(1).getCell(0).getStringCellValue());
            assertTrue(workbook.isSheetHidden(workbook.getSheetIndex(optionSheet)), "选项sheet应被隐藏");

            // 下拉校验改为引用隐藏sheet的区间
            assertEquals(1, sheet.getDataValidations().size());
            DataValidation validation = sheet.getDataValidations().get(0);
            assertTrue(validation.getValidationConstraint().getFormula1().contains("__fc_options__"));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void commaFreeLabelShouldKeepInlineList() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            sheet.createRow(0).createCell(0).setCellValue("性别");

            Map<Integer, List<String>> optionColumns = new LinkedHashMap<>();
            optionColumns.put(0, Arrays.asList("男", "女"));

            CustomSheetWriteHandler.writeOptionValidation(workbook, sheet, optionColumns);

            assertNull(workbook.getSheet("__fc_options__"), "普通选项无需隐藏sheet");
            assertEquals(1, sheet.getDataValidations().size());
            assertTrue(sheet.getDataValidations().get(0).getValidationConstraint().getFormula1().contains("男"));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
