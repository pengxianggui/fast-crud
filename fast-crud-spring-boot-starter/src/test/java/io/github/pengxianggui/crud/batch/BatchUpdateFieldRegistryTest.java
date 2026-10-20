package io.github.pengxianggui.crud.batch;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link BatchUpdateFieldRegistry} 的单元测试，即字段白名单的判定规则。
 * 不依赖 Spring 容器与数据库：用 {@link TableInfoHelper#initTableInfo} 手工初始化实体元数据后，
 * 即可覆盖真实的白名单解析路径。
 */
class BatchUpdateFieldRegistryTest {

    @BeforeAll
    static void initTableInfo() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfo info = TableInfoHelper.initTableInfo(assistant, DemoEntity.class);
        assertNotNull(info, "TableInfo 初始化失败，白名单测试无法进行");
    }

    @Test
    @DisplayName("白名单包含普通业务字段，并解析出正确的数据库列名")
    void whitelistContainsBusinessFields() {
        Map<String, BatchUpdateFieldRegistry.AllowedField> allowed =
                BatchUpdateFieldRegistry.allowedFields(DemoEntity.class);

        assertTrue(allowed.containsKey("name"));
        assertTrue(allowed.containsKey("sex"));
        assertTrue(allowed.containsKey("state"));
        assertTrue(allowed.containsKey("graduated"));

        assertEquals("name", allowed.get("name").getColumn());
        // camelCase 属性名 -> snake_case 列名
        assertEquals("love_name", allowed.get("loveName").getColumn());
        assertEquals("luck_time", allowed.get("luckTime").getColumn());
    }

    @Test
    @DisplayName("主键不在白名单中：批量修改不允许改主键")
    void primaryKeyIsNotAllowed() {
        Map<String, BatchUpdateFieldRegistry.AllowedField> allowed =
                BatchUpdateFieldRegistry.allowedFields(DemoEntity.class);

        assertFalse(allowed.containsKey("id"), "主键必须被排除");
        assertNull(BatchUpdateFieldRegistry.find(DemoEntity.class, "id"));
        assertFalse(BatchUpdateFieldRegistry.isAllowed(DemoEntity.class, "id"));
    }

    @Test
    @DisplayName("@IgnoreBatchUpdate 标注的审计字段不在白名单中")
    void ignoredFieldsAreNotAllowed() {
        assertFalse(BatchUpdateFieldRegistry.isAllowed(DemoEntity.class, "createTime"));
        assertFalse(BatchUpdateFieldRegistry.isAllowed(DemoEntity.class, "updateTime"));
        assertFalse(BatchUpdateFieldRegistry.isAllowed(DemoEntity.class, "password"));

        Map<String, BatchUpdateFieldRegistry.AllowedField> allowed =
                BatchUpdateFieldRegistry.allowedFields(DemoEntity.class);
        assertFalse(allowed.containsKey("createTime"));
        assertFalse(allowed.containsKey("updateTime"));
        assertFalse(allowed.containsKey("password"));
    }

    @Test
    @DisplayName("deniedFields 给出被拒绝的字段及可读原因，便于前端置灰与排障")
    void deniedFieldsCarryReasons() {
        Map<String, String> denied = BatchUpdateFieldRegistry.deniedFields(DemoEntity.class);

        assertTrue(denied.containsKey("id"));
        assertTrue(denied.get("id").contains("主键"));

        assertTrue(denied.containsKey("createTime"));
        assertEquals("创建时间属于审计字段", denied.get("createTime"));

        assertTrue(denied.containsKey("password"));
        assertEquals("密码不允许批量重置", denied.get("password"));
    }

    @Test
    @DisplayName("不存在的字段返回 null —— 这是拒绝注入式字段名的依据")
    void unknownFieldIsRejected() {
        assertNull(BatchUpdateFieldRegistry.find(DemoEntity.class, "notExists"));
        assertNull(BatchUpdateFieldRegistry.find(DemoEntity.class, "sex=1; DROP TABLE demo;--"));
        assertFalse(BatchUpdateFieldRegistry.isAllowed(DemoEntity.class,
                "sex=1; DROP TABLE demo;--"));
        // 传数据库列名同样不认，接口只接受实体属性名
        assertNull(BatchUpdateFieldRegistry.find(DemoEntity.class, "love_name"));
    }

    @Test
    @DisplayName("空字段名返回 null，不抛异常")
    void blankFieldIsSafe() {
        assertNull(BatchUpdateFieldRegistry.find(DemoEntity.class, null));
        assertNull(BatchUpdateFieldRegistry.find(DemoEntity.class, ""));
        assertNull(BatchUpdateFieldRegistry.find(DemoEntity.class, "   "));
    }

    @Test
    @DisplayName("@BatchUpdatable 的 value 作为展示名，未标注时回落到属性名")
    void labelComesFromAnnotation() {
        Map<String, BatchUpdateFieldRegistry.AllowedField> allowed =
                BatchUpdateFieldRegistry.allowedFields(DemoEntity.class);

        assertEquals("性别", allowed.get("sex").getLabel());
        assertEquals("属国", allowed.get("state").getLabel());
        assertEquals("name", allowed.get("name").getLabel());
    }

    @Test
    @DisplayName("describe 输出面向前端的字段清单（field/label/type）")
    void describeProducesFrontendPayload() {
        List<Map<String, Object>> described = BatchUpdateFieldRegistry.describe(DemoEntity.class);

        assertFalse(described.isEmpty());
        Map<String, Object> sex = described.stream()
                .filter(item -> "sex".equals(item.get("field")))
                .findFirst()
                .orElse(null);
        assertNotNull(sex, "describe 结果应包含 sex");
        assertEquals("性别", sex.get("label"));
        assertEquals("String", sex.get("type"));

        // 被禁用的字段不应出现在前端下拉里
        assertFalse(described.stream().anyMatch(item -> "createTime".equals(item.get("field"))));
        assertFalse(described.stream().anyMatch(item -> "id".equals(item.get("field"))));
    }

    @Test
    @DisplayName("bumpVersion 使缓存失效后仍能重新解析出同样的白名单")
    void bumpVersionInvalidatesCache() {
        Map<String, BatchUpdateFieldRegistry.AllowedField> before =
                BatchUpdateFieldRegistry.allowedFields(DemoEntity.class);
        assertTrue(BatchUpdateFieldRegistry.isAllowed(DemoEntity.class, "sex"));

        BatchUpdateFieldRegistry.bumpVersion();

        Map<String, BatchUpdateFieldRegistry.AllowedField> after =
                BatchUpdateFieldRegistry.allowedFields(DemoEntity.class);
        assertEquals(before.keySet(), after.keySet(), "版本失效后重新解析的结果应保持一致");
        assertTrue(BatchUpdateFieldRegistry.isAllowed(DemoEntity.class, "sex"));
    }

    @Test
    @DisplayName("非 MyBatis-Plus 实体返回空白名单，而不是抛异常")
    void nonEntityYieldsEmptyWhitelist() {
        assertTrue(BatchUpdateFieldRegistry.allowedFields(NotAnEntity.class).isEmpty());
        assertFalse(BatchUpdateFieldRegistry.isAllowed(NotAnEntity.class, "anything"));
        assertTrue(BatchUpdateFieldRegistry.describe(NotAnEntity.class).isEmpty());
    }

    // ------------------------------------------------------------------
    // 测试用实体
    // ------------------------------------------------------------------

    /**
     * 覆盖各种白名单情形：普通字段、camelCase 字段、枚举、显式展示名、以及被禁用的审计/敏感字段。
     */
    @TableName("demo_entity")
    static class DemoEntity {
        @TableId(value = "id", type = IdType.AUTO)
        private Long id;

        private String name;

        @BatchUpdatable("性别")
        private String sex;

        @BatchUpdatable("属国")
        private String state;

        private Boolean graduated;

        private String loveName;

        private LocalDateTime luckTime;

        @IgnoreBatchUpdate("创建时间属于审计字段")
        private LocalDateTime createTime;

        @IgnoreBatchUpdate("更新时间由服务端自动维护")
        private LocalDateTime updateTime;

        @IgnoreBatchUpdate("密码不允许批量重置")
        private String password;
    }

    static class NotAnEntity {
        private String anything;
    }
}
