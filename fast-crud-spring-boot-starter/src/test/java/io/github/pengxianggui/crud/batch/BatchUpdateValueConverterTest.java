package io.github.pengxianggui.crud.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link BatchUpdateValueConverter} 的单元测试。
 * 这个类不依赖 Spring 容器，因此只覆盖「类型收敛」这一层的语义：
 * 前端传来的弱类型 JSON 值必须被收敛到实体字段声明的类型上，做不到就明确报错，
 * 而不是把脏值丢给数据库 —— 批量场景下一次误操作会改坏一批数据。
 */
class BatchUpdateValueConverterTest {

    enum Color {
        RED, GREEN, BLUE
    }

    // ------------------------------------------------------------------
    // null 处理
    // ------------------------------------------------------------------

    @Test
    @DisplayName("值为 null 时原样返回，准入判定交给 Service 层（allowNull）")
    void nullPassesThrough() {
        assertNull(BatchUpdateValueConverter.convert(null, String.class, "name"));
        assertNull(BatchUpdateValueConverter.convert(null, Integer.class, "age"));
    }

    @Test
    @DisplayName("目标类型为 Object 时不做转换")
    void objectTargetIsNoop() {
        Object raw = new Object();
        assertEquals(raw, BatchUpdateValueConverter.convert(raw, Object.class, "any"));
        assertNull(BatchUpdateValueConverter.convert(null, Object.class, "any"));
    }

    // ------------------------------------------------------------------
    // 字符串
    // ------------------------------------------------------------------

    @Test
    @DisplayName("数字/布尔收敛为字符串")
    void toStr() {
        assertEquals("18", BatchUpdateValueConverter.convert(18, String.class, "name"));
        assertEquals("true", BatchUpdateValueConverter.convert(true, String.class, "name"));
        assertEquals("0", BatchUpdateValueConverter.convert("0", String.class, "sex"));
    }

    // ------------------------------------------------------------------
    // 数值
    // ------------------------------------------------------------------

    @Test
    @DisplayName("字符串收敛为整数/长整数/小数")
    void toNumbers() {
        assertEquals(18, BatchUpdateValueConverter.convert("18", Integer.class, "age"));
        assertEquals(18, BatchUpdateValueConverter.convert(18L, Integer.class, "age"));
        assertEquals(18L, BatchUpdateValueConverter.convert("18", Long.class, "id"));
        assertEquals(new BigDecimal("1.5"),
                BatchUpdateValueConverter.convert("1.5", BigDecimal.class, "score"));
    }

    @Test
    @DisplayName("非法数值明确报错，并带上字段名")
    void badNumberThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> BatchUpdateValueConverter.convert("abc", Integer.class, "age"));
        assertTrue(ex.getMessage().contains("age"), ex.getMessage());
        assertTrue(ex.getMessage().contains("Integer"), ex.getMessage());
    }

    // ------------------------------------------------------------------
    // 布尔
    // ------------------------------------------------------------------

    @Test
    @DisplayName("布尔接受 true/false/1/0/y/n/yes/no（大小写不敏感）")
    void toBoolean() {
        for (String truthy : Arrays.asList("true", "TRUE", "1", "y", "Y", "yes", "YES")) {
            assertEquals(Boolean.TRUE, BatchUpdateValueConverter.convert(truthy, Boolean.class, "graduated"),
                    "should be true: " + truthy);
        }
        for (String falsy : Arrays.asList("false", "FALSE", "0", "n", "N", "no", "NO")) {
            assertEquals(Boolean.FALSE, BatchUpdateValueConverter.convert(falsy, Boolean.class, "graduated"),
                    "should be false: " + falsy);
        }
    }

    @Test
    @DisplayName("无法识别的布尔值报错")
    void badBooleanThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> BatchUpdateValueConverter.convert("maybe", Boolean.class, "graduated"));
        assertTrue(ex.getMessage().contains("graduated"), ex.getMessage());
    }

    // ------------------------------------------------------------------
    // 枚举
    // ------------------------------------------------------------------

    @Test
    @DisplayName("枚举按名称匹配，大小写不敏感")
    void toEnum() {
        assertEquals(Color.RED, BatchUpdateValueConverter.convert("red", Color.class, "color"));
        assertEquals(Color.BLUE, BatchUpdateValueConverter.convert("BLUE", Color.class, "color"));
    }

    @Test
    @DisplayName("非法枚举值报错并列出可选值")
    void badEnumThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> BatchUpdateValueConverter.convert("PURPLE", Color.class, "color"));
        assertTrue(ex.getMessage().contains("color"), ex.getMessage());
        assertTrue(ex.getMessage().contains("RED"), ex.getMessage());
    }

    // ------------------------------------------------------------------
    // 日期时间
    // ------------------------------------------------------------------

    @Test
    @DisplayName("日期字符串收敛为 LocalDate / LocalDateTime / LocalTime")
    void toDateTypes() {
        assertEquals(LocalDate.of(2007, 3, 15),
                BatchUpdateValueConverter.convert("2007-03-15", LocalDate.class, "birthday"));
        assertEquals(LocalTime.of(8, 30),
                BatchUpdateValueConverter.convert("08:30:00", LocalTime.class, "luckTime"));
        Object dateTime = BatchUpdateValueConverter.convert("2026-01-02T03:04:05", LocalDateTime.class, "updateTime");
        assertTrue(dateTime instanceof LocalDateTime, "expected LocalDateTime but got " + dateTime);
    }

    @Test
    @DisplayName("已是目标类型时直接返回同一实例")
    void alreadyTargetType() {
        LocalDate date = LocalDate.of(2007, 3, 15);
        assertEquals(date, BatchUpdateValueConverter.convert(date, LocalDate.class, "birthday"));
        Integer age = 18;
        assertEquals(age, BatchUpdateValueConverter.convert(age, Integer.class, "age"));
    }

    // ------------------------------------------------------------------
    // 字符
    // ------------------------------------------------------------------

    @Test
    @DisplayName("字符类型接受单字符，拒绝多字符")
    void toCharacter() {
        assertEquals('A', BatchUpdateValueConverter.convert("A", Character.class, "flag"));
        assertThrows(IllegalArgumentException.class,
                () -> BatchUpdateValueConverter.convert("AB", Character.class, "flag"));
    }

    // ------------------------------------------------------------------
    // 复杂度判定（供交互层过滤候选字段）
    // ------------------------------------------------------------------

    @Test
    @DisplayName("isComplexType：集合/数组/自定义对象为复杂类型；String 等常用类型不是")
    void complexTypeDetection() {
        assertTrue(BatchUpdateValueConverter.isComplexType(List.class));
        assertTrue(BatchUpdateValueConverter.isComplexType(String[].class));

        // 回归：Hutool 的 isBasicType(String.class) 返回 false，
        // 若只依赖它会把最常用的 String 字段误判为复杂类型而排除在批量修改之外。
        assertFalse(BatchUpdateValueConverter.isComplexType(String.class),
                "String 不是复杂类型（Hutool isBasicType 对 String 返回 false，必须显式处理）");
        assertFalse(BatchUpdateValueConverter.isComplexType(StringBuilder.class),
                "CharSequence 的实现类同样可直接输入");

        assertFalse(BatchUpdateValueConverter.isComplexType(Integer.class));
        assertFalse(BatchUpdateValueConverter.isComplexType(Boolean.class));
        assertFalse(BatchUpdateValueConverter.isComplexType(java.util.Date.class));
        assertFalse(BatchUpdateValueConverter.isComplexType(LocalDate.class));
        assertFalse(BatchUpdateValueConverter.isComplexType(Color.class));

        // 拿不到类型信息时保守地按复杂类型处理（过滤掉，而不是放行）
        assertTrue(BatchUpdateValueConverter.isComplexType(null));
    }
}
