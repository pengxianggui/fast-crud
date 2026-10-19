package io.github.pengxianggui.crud;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.pengxianggui.crud.query.Cond;
import io.github.pengxianggui.crud.query.ExportParam;
import io.github.pengxianggui.crud.query.Opt;
import io.github.pengxianggui.crud.query.PagerQuery;
import io.github.pengxianggui.crud.query.Query;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 标准导出接口的数据选择逻辑测试。
 */
class BaseReadControllerExportTest {

    @Test
    void selectedExportShouldKeepPageConditionsAndQueryAllSelectedIds() {
        BaseService<ExportRow> baseService = mockBaseService();
        TestController controller = new TestController(baseService);
        PagerQuery pageQuery = new PagerQuery();
        pageQuery.addCond("name", "张三");

        ExportParam exportParam = exportParam(pageQuery);
        exportParam.setSelectedIds(Arrays.asList(1L, 2L));
        exportParam.setIdField("id");
        exportParam.setAll(true);
        List<ExportRow> selectedRows = Arrays.asList(row(1L), row(2L));
        when(baseService.queryList(any(Query.class))).thenReturn(selectedRows);

        assertSame(selectedRows, controller.resolve(exportParam));

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(baseService).queryList(queryCaptor.capture());
        List<Cond> conditions = queryCaptor.getValue().getConds();
        assertEquals(2, conditions.size());
        assertEquals("name", conditions.get(0).getCol());
        assertEquals("id", conditions.get(1).getCol());
        assertEquals(Opt.IN, conditions.get(1).getOpt());
        assertEquals(Arrays.asList(1L, 2L), conditions.get(1).getVal());
        verify(baseService, never()).queryPage(any(PagerQuery.class));
    }

    @Test
    void emptySelectedIdsShouldExportNothingWithoutQuerying() {
        BaseService<ExportRow> baseService = mockBaseService();
        TestController controller = new TestController(baseService);
        ExportParam exportParam = exportParam(new PagerQuery());
        exportParam.setSelectedIds(Collections.emptyList());
        exportParam.setAll(true);

        assertTrue(controller.resolve(exportParam).isEmpty());

        verify(baseService, never()).queryList(any(Query.class));
        verify(baseService, never()).queryPage(any(PagerQuery.class));
    }

    @Test
    void selectedIdsShouldRequireIdField() {
        TestController controller = new TestController(mockBaseService());
        ExportParam exportParam = exportParam(new PagerQuery());
        exportParam.setSelectedIds(Collections.singletonList(1L));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> controller.resolve(exportParam));

        assertEquals("idField can not be blank when selectedIds is not empty", error.getMessage());
    }

    @Test
    void selectedExportShouldCreateDefaultPageQueryWhenMissing() {
        BaseService<ExportRow> baseService = mockBaseService();
        TestController controller = new TestController(baseService);
        ExportParam exportParam = exportParam(null);
        exportParam.setSelectedIds(Collections.singletonList(1L));
        exportParam.setIdField("id");
        List<ExportRow> selectedRows = Collections.singletonList(row(1L));
        when(baseService.queryList(any(Query.class))).thenReturn(selectedRows);

        assertSame(selectedRows, controller.resolve(exportParam));
        assertNotNull(exportParam.getPageQuery());

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(baseService).queryList(queryCaptor.capture());
        assertEquals(1, queryCaptor.getValue().getConds().size());
        Cond condition = queryCaptor.getValue().getConds().get(0);
        assertEquals("id", condition.getCol());
        assertEquals(Opt.IN, condition.getOpt());
        assertEquals(Collections.singletonList(1L), condition.getVal());
    }

    @Test
    void allAndCurrentPageExportsShouldKeepExistingBehavior() {
        BaseService<ExportRow> baseService = mockBaseService();
        TestController controller = new TestController(baseService);
        List<ExportRow> allRows = Collections.singletonList(row(1L));
        Page<ExportRow> currentPage = new Page<>(1, 20, 1);
        currentPage.setRecords(Collections.singletonList(row(2L)));
        when(baseService.queryList(any(Query.class))).thenReturn(allRows);
        when(baseService.queryPage(any(PagerQuery.class))).thenReturn(currentPage);

        ExportParam allParam = exportParam(new PagerQuery());
        allParam.setAll(true);
        assertSame(allRows, controller.resolve(allParam));

        ExportParam pageParam = exportParam(new PagerQuery());
        pageParam.setAll(false);
        assertEquals(Collections.singletonList(row(2L)).size(), controller.resolve(pageParam).size());

        verify(baseService).queryList(any(Query.class));
        verify(baseService).queryPage(any(PagerQuery.class));
    }

    @Test
    void templateExportShouldTakePrecedenceOverSelectedIds() {
        BaseService<ExportRow> baseService = mockBaseService();
        TestController controller = new TestController(baseService);
        ExportParam exportParam = exportParam(new PagerQuery());
        exportParam.setTemplate(true);
        exportParam.setSelectedIds(Collections.singletonList(1L));

        assertTrue(controller.resolve(exportParam).isEmpty());

        verify(baseService, never()).queryList(any(Query.class));
        verify(baseService, never()).queryPage(any(PagerQuery.class));
    }

    @SuppressWarnings("unchecked")
    private static BaseService<ExportRow> mockBaseService() {
        BaseService<ExportRow> baseService = Mockito.mock(BaseService.class);
        when(baseService.getEntityClass()).thenReturn(ExportRow.class);
        return baseService;
    }

    private static ExportParam exportParam(PagerQuery pageQuery) {
        ExportParam exportParam = new ExportParam();
        exportParam.setColumns(Collections.singletonList(Collections.singletonMap("col", "name")));
        exportParam.setPageQuery(pageQuery);
        return exportParam;
    }

    private static ExportRow row(long id) {
        ExportRow row = new ExportRow();
        row.setId(id);
        row.setName("记录" + id);
        return row;
    }

    static class TestController extends BaseReadController<ExportRow> {
        TestController(BaseService<ExportRow> baseService) {
            super(baseService, ExportRow.class);
        }

        List<ExportRow> resolve(ExportParam exportParam) {
            return resolveExportData(exportParam);
        }
    }

    @Data
    static class ExportRow {
        private Long id;
        private String name;
    }
}
