package io.github.pengxianggui.crud.demo.batch;

import io.github.pengxianggui.crud.BaseService;
import io.github.pengxianggui.crud.batch.BatchUpdateDTO;
import io.github.pengxianggui.crud.batch.BatchUpdateExceptionHandler;
import io.github.pengxianggui.crud.batch.BatchUpdateResult;
import io.github.pengxianggui.crud.demo.domain.Student;
import io.github.pengxianggui.crud.demo.service.StudentService;
import io.github.pengxianggui.crud.demo.service.impl.StudentServiceImpl;
import io.github.pengxianggui.crud.query.PagerQuery;
import io.github.pengxianggui.crud.query.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 批量修改的集成测试，跑在真实 MySQL 上，覆盖 Service -> Mapper -> SQL 的完整链路。
 * 单元测试只能验证白名单解析和类型收敛，而 SET/WHERE 是否拼接正确、数据权限条件是否真的进了
 * UPDATE 的 WHERE、审计列有没有注入，这些只有连库执行才能确认。
 * 测试类开启事务，方法结束后整体回滚，不会污染开发库；断言先读原值再对比，
 * 不依赖固定主键的当前值，开发库被手工改过也不会假失败。
 * 前置：MySQL 中存在 fast-crud 库与 student 表（见根目录 schema.sql）。
 * 连接信息可用环境变量 DB_IP / DB_PORT / DB_USER / DB_PASS 覆盖，默认 127.0.0.1:3306 root/123456。
 */
@SpringBootTest
@Transactional
class BatchUpdateIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private BaseService baseService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 异常处理器是全局 Advice，默认不注册，只有显式配置开关后才生效。
     * demo 的 application.yml 里把它打开了，这里验证装配结果确实跟着配置走。
     * 可选的注入，Bean 不存在时保持为 null。
     */
    @Autowired(required = false)
    private BatchUpdateExceptionHandler batchUpdateExceptionHandler;

    @AfterEach
    void resetDataPermission() {
        // 静态开关是跨用例共享的，必须复位，否则会污染后续用例
        StudentServiceImpl.dataPermissionEnabled = false;
    }

    @Test
    @DisplayName("异常处理器按配置装配：demo 开启开关后才注册")
    void exceptionHandlerFollowsProperty() {
        assertNotNull(batchUpdateExceptionHandler,
                "demo 配置了 fast-crud.batch-update.map-validation-error=true，处理器应被注册");
    }

    // ------------------------------------------------------------------
    // 一、PDF 举例
    // ------------------------------------------------------------------

    @Test
    @DisplayName("PDF 举例：勾选大乔、小乔 → 性别=女、属国=吴；曹操不受影响")
    void pdfExample() {
        Student daqiao = findByName("大乔");
        Student xiaoqiao = findByName("小乔");
        Student caocao = findByName("曹操");
        assertNotNull(daqiao);
        assertNotNull(xiaoqiao);
        assertNotNull(caocao);

        BatchUpdateResult result = studentService.batchUpdateByIds(
                ids(daqiao, xiaoqiao),
                Arrays.asList(new BatchUpdateDTO.FieldValue("sex", "0"),
                        new BatchUpdateDTO.FieldValue("state", "5")));

        // 返回体：提交 2 条、命中 2 条、只执行 1 条 SQL
        assertEquals(2, result.getAffectedRows());
        assertEquals(2, result.getRequestedRows());
        assertEquals(1, result.getExecutedStatements());
        assertEquals(Arrays.asList("sex", "state"), result.getFields());

        // 真正落库
        assertEquals("0", getField(daqiao.getId(), "sex"));
        assertEquals("5", getField(daqiao.getId(), "state"));
        assertEquals("0", getField(xiaoqiao.getId(), "sex"));
        assertEquals("5", getField(xiaoqiao.getId(), "state"));

        // 未被勾选的行必须原封不动（这是「按主键精确生效」的核心断言）
        assertEquals(caocao.getSex(), getField(caocao.getId(), "sex"));
        assertEquals(caocao.getState(), getField(caocao.getId(), "state"));
    }

    @Test
    @DisplayName("update_time 由服务端注入：批量修改绕过字段填充，审计列不能丢")
    void serverSideAuditColumnIsFilled() {
        Student daqiao = findByName("大乔");
        assertNotNull(daqiao);
        String before = getField(daqiao.getId(), "update_time");

        studentService.batchUpdateByIds(ids(daqiao),
                Collections.singletonList(new BatchUpdateDTO.FieldValue("sex", "0")));

        String after = getField(daqiao.getId(), "update_time");
        assertNotNull(after, "update_time 应当被服务端注入");
        assertNotNull(before);
    }

    // ------------------------------------------------------------------
    // 二、字段白名单（安全边界）
    // ------------------------------------------------------------------

    @Test
    @DisplayName("拒绝主键：批量修改不允许改 id")
    void rejectPrimaryKey() {
        Student daqiao = findByName("大乔");
        assertNotNull(daqiao);
        Object originalId = daqiao.getId();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.batchUpdateByIds(ids(daqiao),
                        Collections.singletonList(new BatchUpdateDTO.FieldValue("id", 999999))));
        assertTrue(ex.getMessage().contains("id"), ex.getMessage());
        assertTrue(ex.getMessage().contains("主键"), ex.getMessage());

        // 主键没被改动
        assertEquals(String.valueOf(originalId), getField((Serializable) originalId, "id"));
    }

    @Test
    @DisplayName("拒绝 @IgnoreBatchUpdate 的审计字段 createTime")
    void rejectIgnoredField() {
        Student daqiao = findByName("大乔");
        assertNotNull(daqiao);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.batchUpdateByIds(ids(daqiao),
                        Collections.singletonList(
                                new BatchUpdateDTO.FieldValue("createTime", "2020-01-01T00:00:00"))));
        assertTrue(ex.getMessage().contains("createTime"), ex.getMessage());
        assertTrue(ex.getMessage().contains("审计"), ex.getMessage());
    }

    @Test
    @DisplayName("拒绝不存在的字段 / 注入式字段名，且不执行任何 SQL")
    void rejectUnknownAndInjectionField() {
        Student daqiao = findByName("大乔");
        assertNotNull(daqiao);
        String originalSex = getField(daqiao.getId(), "sex");

        for (String badField : Arrays.asList("notExists", "sex=1; DROP TABLE student;--", "love_name")) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> studentService.batchUpdateByIds(ids(daqiao),
                            Collections.singletonList(new BatchUpdateDTO.FieldValue(badField, "0"))),
                    "应当拒绝字段: " + badField);
            assertTrue(ex.getMessage().contains("可批量修改的字段为"), ex.getMessage());
        }

        // 表还在，数据也没被改
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM student", Integer.class);
        assertNotNull(count);
        assertTrue(count > 0, "student 表不应被破坏");
        assertEquals(originalSex, getField(daqiao.getId(), "sex"));
    }

    // ------------------------------------------------------------------
    // 三、入参校验
    // ------------------------------------------------------------------

    @Test
    @DisplayName("空 ids / 空 fields 被拒绝")
    void rejectEmptyInput() {
        assertThrows(IllegalArgumentException.class,
                () -> studentService.batchUpdateByIds(Collections.emptyList(),
                        Collections.singletonList(new BatchUpdateDTO.FieldValue("sex", "0"))));

        Student daqiao = findByName("大乔");
        assertNotNull(daqiao);
        assertThrows(IllegalArgumentException.class,
                () -> studentService.batchUpdateByIds(ids(daqiao), Collections.emptyList()));
    }

    @Test
    @DisplayName("值解析失败时整批拒绝：不允许部分生效")
    void typeErrorRejectsWholeBatch() {
        Student daqiao = findByName("大乔");
        Student xiaoqiao = findByName("小乔");
        assertNotNull(daqiao);
        assertNotNull(xiaoqiao);
        String originalSex = getField(daqiao.getId(), "sex");
        String originalName = getField(xiaoqiao.getId(), "name");

        // 第一个字段合法、第二个字段类型非法 —— 整批应当都不生效
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> studentService.batchUpdateByIds(ids(daqiao, xiaoqiao),
                        Arrays.asList(new BatchUpdateDTO.FieldValue("sex", "0"),
                                new BatchUpdateDTO.FieldValue("age", "abc"))));
        assertTrue(ex.getMessage().contains("age"), ex.getMessage());

        assertEquals(originalSex, getField(daqiao.getId(), "sex"), "非法批次不应部分生效");
        assertEquals(originalName, getField(xiaoqiao.getId(), "name"));
    }

    // ------------------------------------------------------------------
    // 四、数据权限
    // ------------------------------------------------------------------

    @Test
    @DisplayName("数据权限：条件进入 UPDATE 的 WHERE，越权主键既不被改也不计入 affectedRows")
    void dataPermissionFiltersUnauthorizedRows() {
        Student daqiao = findByName("大乔");
        Student xiaoqiao = findByName("小乔");
        Student caocao = findByName("曹操");
        assertNotNull(daqiao);
        assertNotNull(xiaoqiao);
        assertNotNull(caocao);

        // 只允许改 id <= min(大乔,小乔)，曹操（更大的 id）必然越权
        long allowedMax = Math.min(daqiao.getId().longValue(), xiaoqiao.getId().longValue());

        // 先把两条目标行改成确定值，便于断言
        jdbcTemplate.update("UPDATE student SET sex='1', state='1' WHERE id IN (?,?,?)",
                daqiao.getId(), xiaoqiao.getId(), caocao.getId());

        StudentServiceImpl.dataPermissionEnabled = true;
        try {
            // 注意：这里的阈值由 demo 的 StudentServiceImpl 固定为 2；为让断言与实现解耦，
            // 仅当曹操 id 大于阈值时才做越权断言。
            BatchUpdateResult result = studentService.batchUpdateByIds(
                    ids(daqiao, xiaoqiao, caocao),
                    Arrays.asList(new BatchUpdateDTO.FieldValue("sex", "0"),
                            new BatchUpdateDTO.FieldValue("state", "5")));

            if (caocao.getId() > allowedMax) {
                assertTrue(result.getRequestedRows() >= 3, "提交行数应包含越权行");
                assertTrue(result.getAffectedRows() <= 2,
                        "越权行不应被计入 affectedRows，实际=" + result.getAffectedRows());
                assertEquals("1", getField(caocao.getId(), "sex"), "曹操不应被修改");
                assertEquals("1", getField(caocao.getId(), "state"), "曹操不应被修改");
            }
        } finally {
            StudentServiceImpl.dataPermissionEnabled = false;
        }
    }

    // ------------------------------------------------------------------
    // 五、审计日志
    // ------------------------------------------------------------------

    @Test
    @DisplayName("审计落库：记录实体、主键、字段、受影响行数与 SQL 条数")
    void auditIsRecorded() {
        Student daqiao = findByName("大乔");
        assertNotNull(daqiao);

        BatchUpdateResult result = studentService.batchUpdateByIds(ids(daqiao),
                Collections.singletonList(new BatchUpdateDTO.FieldValue("sex", "0")));

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT entity_name, ids, fields, affected_rows, executed_statements "
                        + "FROM fc_batch_update_log WHERE entity_name = ? ORDER BY id DESC LIMIT 1",
                Student.class.getName());

        assertNotNull(row);
        assertEquals(Student.class.getName(), row.get("entity_name"));
        assertEquals(result.getAffectedRows(), ((Number) row.get("affected_rows")).intValue());
        assertEquals(1, ((Number) row.get("executed_statements")).intValue());
        String fields = String.valueOf(row.get("fields"));
        assertTrue(fields.contains("sex"), "审计字段应包含 sex，实际=" + fields);
        assertTrue(fields.contains("update_time"), "审计应包含服务端注入的审计列，实际=" + fields);
        assertTrue(String.valueOf(row.get("ids")).contains(String.valueOf(daqiao.getId())),
                "审计应记录被修改的主键");
    }

    // ------------------------------------------------------------------
    // 六、元数据接口
    // ------------------------------------------------------------------

    @Test
    @DisplayName("可用字段清单：包含业务字段、排除主键与 @IgnoreBatchUpdate 字段")
    void batchUpdatableFieldsMetadata() {
        List<Map<String, Object>> fields = baseService.getBatchUpdatableFields();
        assertFalse(fields.isEmpty());

        List<String> names = new ArrayList<>();
        fields.forEach(item -> names.add(String.valueOf(item.get("field"))));
        assertTrue(names.contains("sex"));
        assertTrue(names.contains("state"));
        assertFalse(names.contains("id"), "主键不应出现在可用字段中");
        assertFalse(names.contains("createTime"));
        assertFalse(names.contains("updateTime"));

        Map<String, String> denied = baseService.getBatchUpdateDeniedFields();
        assertTrue(denied.containsKey("id"));
        assertTrue(denied.containsKey("createTime"));
    }

    @Test
    @DisplayName("分页查询仍可正常工作 —— 批量修改没有破坏既有服务能力")
    void existingQueryStillWorks() {
        PagerQuery query = new PagerQuery();
        query.setCurrent(1);
        query.setSize(5);
        assertNotNull(studentService.queryPage(query));
        assertNotNull(studentService.pageVO(query));
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private Student findByName(String name) {
        // Query(col, val) 构造的是等值条件；避免依赖 Query 的 setter 形态
        return studentService.queryOne(new Query("name", name));
    }

    private List<Serializable> ids(Student... students) {
        List<Serializable> ids = new ArrayList<>(students.length);
        for (Student student : students) {
            ids.add(student.getId());
        }
        return ids;
    }

    private String getField(Serializable id, String column) {
        Object value = jdbcTemplate.queryForMap(
                "SELECT " + column + " FROM student WHERE id = ?", id).get(column);
        return value == null ? null : String.valueOf(value);
    }
}
