package io.github.pengxianggui.crud.importer;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.pengxianggui.crud.FastCrudProperty;
import lombok.Data;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 导入解析与列处理器转换的单元测试(不依赖Spring容器与数据库)。
 * <p>
 * 测试数据用POI直接生成xlsx(表头即导出模板的label), 再交由{@link ExcelImportManager}解析。
 *
 * @author pengxg
 */
class ExcelImportManagerTest {

    @BeforeAll
    static void setUpSpringUtil() {
        // ColumnHandler中通过SpringUtil获取FastCrudProperty(host), 此处提供一个最小上下文
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean(FastCrudProperty.class, FastCrudProperty::new);
        context.refresh();
        new SpringUtil().setApplicationContext(context);
    }

    @Test
    void parseShouldConvertCellValuesByColumnHandler() throws IOException {
        byte[] excel = excel(new String[]{"姓名", "年龄", "性别", "生日"},
                new Object[][]{
                        {"张三", 18, "男", "2006-05-01"},
                        {"李四", 20, "0", "2007-06-02"},
                });

        ImportParseResult<Student> result = parse(excel, columns());

        assertTrue(result.getErrors().isEmpty(), "不应有解析错误: " + result.getErrors());
        assertEquals(2, result.getRows().size());

        ImportRow<Student> first = result.getRows().get(0);
        assertEquals(2, first.getRow(), "首行数据应为excel第2行");
        assertEquals("张三", first.getModel().getName());
        assertEquals(18, first.getModel().getAge());
        assertEquals("1", first.getModel().getSex(), "下拉列应按label反查value");
        assertEquals(LocalDate.of(2006, 5, 1), first.getModel().getBirthday());

        ImportRow<Student> second = result.getRows().get(1);
        assertEquals(3, second.getRow());
        assertEquals("0", second.getModel().getSex(), "下拉列也应支持直接填value");
    }

    @Test
    void parseShouldSupportDayjsStyleDateFormat() throws IOException {
        List<Map<String, Object>> columns = new ArrayList<>();
        Map<String, Object> dateProps = new HashMap<>();
        dateProps.put("value-format", "YYYY-MM-DD"); // 前端dayjs风格
        columns.add(column("birthday", "生日", "fast-table-column-date-picker", dateProps));

        byte[] excel = excel(new String[]{"生日"}, new Object[][]{{"2006-05-01"}});
        ImportParseResult<Student> result = parse(excel, columns);

        assertTrue(result.getErrors().isEmpty());
        assertEquals(LocalDate.of(2006, 5, 1), result.getRows().get(0).getModel().getBirthday());
    }

    @Test
    void parseShouldSkipBlankRowAndIgnoreUnknownHead() throws IOException {
        byte[] excel = excel(new String[]{"姓名", "备注", "年龄"},
                new Object[][]{
                        {"王五", "unknown head", 20}, // "备注"未在列配置中 -> 忽略
                        {"", "", null}, // 空行 -> 跳过
                        {"赵六", "x", 22},
                });

        ImportParseResult<Student> result = parse(excel, columns());

        assertTrue(result.getErrors().isEmpty(), "不应有解析错误: " + result.getErrors());
        assertEquals(2, result.getRows().size());
        assertEquals("王五", result.getRows().get(0).getModel().getName());
        assertEquals(20, result.getRows().get(0).getModel().getAge());
        assertEquals(4, result.getRows().get(1).getRow(), "空行跳过后行号仍应保持excel真实行号");
        assertEquals("赵六", result.getRows().get(1).getModel().getName());
    }

    @Test
    void parseShouldReportRowErrorForInvalidCellValue() throws IOException {
        byte[] excel = excel(new String[]{"姓名", "年龄", "性别"},
                new Object[][]{
                        {"李四", "not-a-number", "男"},
                        {"王五", 20, "未知选项"},
                        {"赵六", 22, "女"},
                });

        ImportParseResult<Student> result = parse(excel, columns());

        assertEquals(2, result.getErrors().size(), "应有两处行错误: " + result.getErrors());
        assertEquals(2, result.getErrors().get(0).getRow());
        assertEquals("年龄", result.getErrors().get(0).getLabel());
        assertEquals(3, result.getErrors().get(1).getRow());
        assertEquals("性别", result.getErrors().get(1).getLabel());
        // 出错的行不进入待导入列表, 正常的行仍然保留
        assertEquals(1, result.getRows().size());
        assertEquals("赵六", result.getRows().get(0).getModel().getName());
    }

    @Test
    void parseShouldExcludeNotImportableColumn() throws IOException {
        List<Map<String, Object>> columns = columns();
        columns.get(0).put("importable", false); // 姓名不参与导入

        byte[] excel = excel(new String[]{"姓名", "年龄"}, new Object[][]{{"张三", 18}});
        ImportParseResult<Student> result = parse(excel, columns);

        assertTrue(result.getErrors().isEmpty());
        assertEquals(1, result.getRows().size());
        assertNull(result.getRows().get(0).getModel().getName(), "importable=false的列不应被导入");
        assertEquals(18, result.getRows().get(0).getModel().getAge());
    }

    private static ImportParseResult<Student> parse(byte[] excel, List<Map<String, Object>> columns) {
        return new ExcelImportManager(objectMapper())
                .parse(new ByteArrayInputStream(excel), columns, Student.class);
    }

    private static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

    private static byte[] excel(String[] head, Object[][] data) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row headRow = sheet.createRow(0);
            for (int i = 0; i < head.length; i++) {
                headRow.createCell(i).setCellValue(head[i]);
            }
            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < data[i].length; j++) {
                    Object value = data[i][j];
                    if (value == null) {
                        continue;
                    }
                    if (value instanceof Number) {
                        row.createCell(j).setCellValue(((Number) value).doubleValue());
                    } else {
                        row.createCell(j).setCellValue(String.valueOf(value));
                    }
                }
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private static List<Map<String, Object>> columns() {
        List<Map<String, Object>> columns = new ArrayList<>();
        columns.add(column("name", "姓名", "fast-table-column-input", new HashMap<>()));
        columns.add(column("age", "年龄", "fast-table-column-number", new HashMap<>()));

        Map<String, Object> selectProps = new HashMap<>();
        selectProps.put("valKey", "value");
        selectProps.put("labelKey", "label");
        List<Map<String, Object>> options = new ArrayList<>();
        options.add(option("男", "1"));
        options.add(option("女", "0"));
        selectProps.put("options", options);
        columns.add(column("sex", "性别", "fast-table-column-select", selectProps));

        Map<String, Object> dateProps = new HashMap<>();
        dateProps.put("value-format", "yyyy-MM-dd");
        columns.add(column("birthday", "生日", "fast-table-column-date-picker", dateProps));
        return columns;
    }

    private static Map<String, Object> column(String col, String label, String component, Map<String, Object> props) {
        Map<String, Object> column = new HashMap<>();
        column.put("col", col);
        column.put("label", label);
        column.put("tableColumnComponentName", component);
        column.put("importable", true);
        column.put("props", props);
        return column;
    }

    private static Map<String, Object> option(String label, String value) {
        Map<String, Object> option = new HashMap<>();
        option.put("label", label);
        option.put("value", value);
        return option;
    }

    @Data
    public static class Student {
        private String name;
        private Integer age;
        private String sex;
        private LocalDate birthday;
    }
}
