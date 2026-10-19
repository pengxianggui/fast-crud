package io.github.pengxianggui.crud.export;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.pengxianggui.crud.FastCrudProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.Ordered;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 自定义Java类型导出值转换测试。
 */
class ExcelValueConverterTest {

    @BeforeAll
    static void setUpSpringUtil() {
        System.setProperty("java.awt.headless", "true");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean(FastCrudProperty.class, FastCrudProperty::new);
        context.refresh();
        new SpringUtil().setApplicationContext(context);
    }

    @Test
    void interfaceFieldAndInheritedFieldShouldUseConverter() throws Exception {
        FileRow row = new FileRow();
        row.setFile(new TestFile("nprf.pdf"));
        row.setInheritedFile(new TestFile("history.pdf"));

        try (XSSFWorkbook workbook = export(
                Collections.singletonList(row),
                Arrays.asList(column("file"), column("inheritedFile")),
                Collections.singletonList(new FileValueConverter())
        )) {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals("nprf.pdf", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("history.pdf", sheet.getRow(1).getCell(1).getStringCellValue());
        }
    }

    @Test
    void convertersShouldBeSortedByOrderBeforeMatching() throws Exception {
        FileRow row = new FileRow();
        row.setFile(new TestFile("origin.pdf"));

        try (XSSFWorkbook workbook = export(
                Collections.singletonList(row),
                Collections.singletonList(column("file")),
                Arrays.asList(
                        new FixedNameConverter(2, "second.pdf"),
                        new FixedNameConverter(1, "first.pdf")
                )
        )) {
            assertEquals("first.pdf", workbook.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());
        }
    }

    @Test
    void serializedValueShouldBeKeptWhenNoConverterMatches() throws Exception {
        PlainRow row = new PlainRow();
        row.setValue(new PlainValue("fallback"));

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(PlainValue.class, new JsonSerializer<PlainValue>() {
            @Override
            public void serialize(PlainValue value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(value.getName());
            }
        });
        objectMapper.registerModule(module);
        try (XSSFWorkbook workbook = export(
                Collections.singletonList(row),
                Collections.singletonList(column("value")),
                Collections.emptyList(),
                objectMapper
        )) {
            String value = workbook.getSheetAt(0).getRow(1).getCell(0).getStringCellValue();
            assertTrue(value.contains("fallback"), "无匹配转换器时应保留Jackson序列化结果");
        }
    }

    @Test
    void unsupportedSerializedValueShouldExportBlankCellInsteadOfFailing() throws Exception {
        PlainRow row = new PlainRow();
        row.setValue(new PlainValue("unsupported"));

        try (XSSFWorkbook workbook = export(
                Collections.singletonList(row),
                Collections.singletonList(column("value")),
                Collections.emptyList()
        )) {
            Cell cell = workbook.getSheetAt(0).getRow(1).getCell(0);
            assertTrue(cell == null
                    || cell.getCellType() == CellType.BLANK
                    || (cell.getCellType() == CellType.STRING && cell.getStringCellValue().isEmpty()));
        }
    }

    @Test
    void nullValueShouldExportBlankCell() throws Exception {
        FileRow row = new FileRow();

        try (XSSFWorkbook workbook = export(
                Collections.singletonList(row),
                Collections.singletonList(column("file")),
                Collections.singletonList(new FileValueConverter())
        )) {
            Cell cell = workbook.getSheetAt(0).getRow(1).getCell(0);
            assertTrue(cell == null
                    || cell.getCellType() == CellType.BLANK
                    || (cell.getCellType() == CellType.STRING && cell.getStringCellValue().isEmpty()));
        }
    }

    @Test
    void emptyDataShouldNotInvokeConverter() throws Exception {
        AtomicInteger invocationCount = new AtomicInteger();
        ExcelValueConverter<FileValue> converter = new FileValueConverter() {
            @Override
            public Object convert(FileValue value) {
                invocationCount.incrementAndGet();
                return super.convert(value);
            }
        };

        try (XSSFWorkbook workbook = export(
                Collections.emptyList(),
                Collections.singletonList(column("file")),
                Collections.singletonList(converter)
        )) {
            assertEquals(0, invocationCount.get());
            assertEquals("File", workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        }
    }

    @Test
    void converterExceptionShouldFailExport() {
        FileRow row = new FileRow();
        row.setFile(new TestFile("nprf.pdf"));
        ExcelValueConverter<FileValue> converter = new ExcelValueConverter<FileValue>() {
            @Override
            public Class<FileValue> supportedType() {
                return FileValue.class;
            }

            @Override
            public Object convert(FileValue value) {
                throw new IllegalStateException("convert failed");
            }
        };

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> export(Collections.singletonList(row), Collections.singletonList(column("file")),
                        Collections.singletonList(converter)));
        assertEquals("convert failed", error.getMessage());
    }

    private static XSSFWorkbook export(List<?> rows, List<Map<String, Object>> columns,
                                       List<ExcelValueConverter<?>> converters) throws Exception {
        return export(rows, columns, converters, new ObjectMapper());
    }

    private static XSSFWorkbook export(List<?> rows, List<Map<String, Object>> columns,
                                       List<ExcelValueConverter<?>> converters,
                                       ObjectMapper objectMapper) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new ExcelExportManager(objectMapper, converters)
                .exportByConfig(rows, columns, outputStream);
        return new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()));
    }

    private static Map<String, Object> column(String col) {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("col", col);
        column.put("label", "File");
        column.put("exportable", true);
        column.put("tableColumnComponentName", "FastTableColumn");
        column.put("props", Collections.emptyMap());
        return column;
    }

    interface FileValue {
        String getFilename();
    }

    @Getter
    @Setter
    static class TestFile implements FileValue {
        private String filename;

        TestFile(String filename) {
            this.filename = filename;
        }
    }

    static class FileValueConverter implements ExcelValueConverter<FileValue> {
        @Override
        public Class<FileValue> supportedType() {
            return FileValue.class;
        }

        @Override
        public Object convert(FileValue value) {
            return value.getFilename();
        }
    }

    static class FixedNameConverter implements ExcelValueConverter<FileValue>, Ordered {
        private final int order;
        private final String filename;

        FixedNameConverter(int order, String filename) {
            this.order = order;
            this.filename = filename;
        }

        @Override
        public Class<FileValue> supportedType() {
            return FileValue.class;
        }

        @Override
        public Object convert(FileValue value) {
            return filename;
        }

        @Override
        public int getOrder() {
            return order;
        }
    }

    @Getter
    @Setter
    static class BaseFileRow {
        private FileValue inheritedFile;
    }

    @Getter
    @Setter
    static class FileRow extends BaseFileRow {
        private FileValue file;
    }

    @Getter
    @Setter
    static class PlainRow {
        private PlainValue value;
    }

    @Getter
    @Setter
    static class PlainValue {
        private String name;

        PlainValue(String name) {
            this.name = name;
        }
    }
}
