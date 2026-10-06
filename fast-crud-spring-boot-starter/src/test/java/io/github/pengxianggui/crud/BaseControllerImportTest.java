package io.github.pengxianggui.crud;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pengxianggui.crud.importer.ImportResult;
import io.github.pengxianggui.crud.importer.ImportRow;
import lombok.Data;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 导入接口的请求绑定测试: 验证columns/extra以body中的json部分(part=param)提交, 文件通过part=file提交。
 *
 * @author pengxg
 */
class BaseControllerImportTest {

    @BeforeAll
    static void setUpSpringUtil() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean(FastCrudProperty.class, FastCrudProperty::new);
        context.refresh();
        new SpringUtil().setApplicationContext(context);
    }

    @Test
    void importShouldBindJsonParamPartAndDelegateToService() throws Exception {
        BaseService baseService = Mockito.mock(BaseService.class);
        Mockito.when(baseService.getEntityClass()).thenReturn((Class) Foo.class);
        Mockito.when(baseService.importData(anyList(), any(), anyList(), anyMap()))
                .thenReturn(ImportResult.success(1, 1, 0));

        TestController controller = new TestController(baseService);
        ReflectionTestUtils.setField(controller, "objectMapper", objectMapper());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        MockMultipartFile param = new MockMultipartFile("param", "param.json", "application/json",
                paramJson().getBytes(StandardCharsets.UTF_8));
        MockMultipartFile file = new MockMultipartFile("file", "foo.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excel());

        mockMvc.perform(multipart("/test/import").file(param).file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.inserted").value(1))
                .andExpect(jsonPath("$.updated").value(0));

        ArgumentCaptor<List> rowsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> columnsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map> extraCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(baseService).importData(rowsCaptor.capture(), any(), columnsCaptor.capture(), extraCaptor.capture());

        assertEquals(1, rowsCaptor.getValue().size());
        ImportRow<?> row = (ImportRow<?>) rowsCaptor.getValue().get(0);
        assertEquals(2, row.getRow());
        assertEquals("张三", ((Foo) row.getModel()).getName());
        assertEquals(1, columnsCaptor.getValue().size());
        assertEquals(7, extraCaptor.getValue().get("customerId"));
    }

    private static String paramJson() {
        return "{\"columns\":[{\"col\":\"name\",\"label\":\"姓名\",\"tableColumnComponentName\":"
                + "\"fast-table-column-input\",\"importable\":true,\"props\":{}}],"
                + "\"extra\":{\"customerId\":7}}";
    }

    private static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

    private static byte[] excel() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row head = sheet.createRow(0);
            head.createCell(0).setCellValue("姓名");
            Row data = sheet.createRow(1);
            data.createCell(0).setCellValue("张三");
            workbook.write(out);
            return out.toByteArray();
        }
    }

    @RestController
    @RequestMapping("test")
    static class TestController extends BaseController<Foo> {
        TestController(BaseService baseService) {
            super(baseService, Foo.class);
        }
    }

    @Data
    public static class Foo {
        private String name;
    }
}
