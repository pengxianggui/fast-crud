package io.github.pengxianggui.crud.demo.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import io.github.pengxianggui.crud.util.DateConvertUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class DateConvertUtilTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        SimpleModule module = new SimpleModule();
        module.addSerializer(OffsetDateTime.class, OffsetDateTimeSerializer.INSTANCE);
        module.addDeserializer(OffsetDateTime.class, InstantDeserializer.OFFSET_DATE_TIME);
        module.addSerializer(ZonedDateTime.class, ZonedDateTimeSerializer.INSTANCE);
        module.addDeserializer(ZonedDateTime.class, InstantDeserializer.ZONED_DATE_TIME);
        module.addSerializer(Instant.class, InstantSerializer.INSTANCE);
        module.addDeserializer(Instant.class, InstantDeserializer.INSTANT);
        objectMapper.registerModule(module);

        objectMapper.setTimeZone(TimeZone.getDefault());
    }

    // ======================== isDateType ========================

    @Test
    @DisplayName("isDateType - java.time 类型应返回 true")
    public void testIsDateType_javaTime() {
        Assertions.assertTrue(DateConvertUtil.isDateType(LocalDateTime.class));
        Assertions.assertTrue(DateConvertUtil.isDateType(LocalDate.class));
        Assertions.assertTrue(DateConvertUtil.isDateType(LocalTime.class));
        Assertions.assertTrue(DateConvertUtil.isDateType(OffsetDateTime.class));
        Assertions.assertTrue(DateConvertUtil.isDateType(ZonedDateTime.class));
        Assertions.assertTrue(DateConvertUtil.isDateType(Instant.class));
    }

    @Test
    @DisplayName("isDateType - java.util.Date 及其子类应返回 true")
    public void testIsDateType_javaUtilDate() {
        Assertions.assertTrue(DateConvertUtil.isDateType(java.util.Date.class));
        Assertions.assertTrue(DateConvertUtil.isDateType(java.sql.Date.class));
        Assertions.assertTrue(DateConvertUtil.isDateType(java.sql.Timestamp.class));
        Assertions.assertTrue(DateConvertUtil.isDateType(java.sql.Time.class));
    }

    @Test
    @DisplayName("isDateType - Calendar 应返回 true")
    public void testIsDateType_calendar() {
        Assertions.assertTrue(DateConvertUtil.isDateType(Calendar.class));
    }

    @Test
    @DisplayName("isDateType - 非日期类型应返回 false")
    public void testIsDateType_nonDate() {
        Assertions.assertFalse(DateConvertUtil.isDateType(String.class));
        Assertions.assertFalse(DateConvertUtil.isDateType(Integer.class));
        Assertions.assertFalse(DateConvertUtil.isDateType(Long.class));
        Assertions.assertFalse(DateConvertUtil.isDateType(Object.class));
    }

    // ======================== 边界情况 ========================

    @Test
    @DisplayName("convertToDate - 非日期类型原值返回")
    public void testNonDateType_notConverted() {
        Assertions.assertEquals("hello", DateConvertUtil.convertToDate("hello", String.class, objectMapper));
        Assertions.assertEquals(123, DateConvertUtil.convertToDate(123, Integer.class, objectMapper));
    }

    @Test
    @DisplayName("convertToDate - null 原值返回")
    public void testNull_notConverted() {
        Assertions.assertNull(DateConvertUtil.convertToDate(null, LocalDateTime.class, objectMapper));
    }

    @Test
    @DisplayName("convertToDate - 空白字符串原值返回")
    public void testBlankString_notConverted() {
        Assertions.assertEquals("", DateConvertUtil.convertToDate("", LocalDateTime.class, objectMapper));
        Assertions.assertEquals("  ", DateConvertUtil.convertToDate("  ", LocalDateTime.class, objectMapper));
    }

    @Test
    @DisplayName("convertToDate - 已是日期类型的值原值返回")
    public void testAlreadyDateType_notConverted() {
        LocalDate now = LocalDate.now();
        Assertions.assertSame(now, DateConvertUtil.convertToDate(now, LocalDate.class, objectMapper));
    }

    @Test
    @DisplayName("convertToDate - 非字符串非集合值原值返回")
    public void testNonStringNonCollection_notConverted() {
        Integer num = 12345;
        Assertions.assertSame(num, DateConvertUtil.convertToDate(num, LocalDateTime.class, objectMapper));
    }

    @Test
    @DisplayName("convertToDate - 无法解析的字符串原值返回")
    public void testUnparseableString_notConverted() {
        Object result = DateConvertUtil.convertToDate("not-a-date", LocalDateTime.class, objectMapper);
        Assertions.assertEquals("not-a-date", result);
    }

    // ======================== LocalDateTime ========================

    @Test
    @DisplayName("convertToDate - ISO 格式字符串转 LocalDateTime")
    public void testStringToLocalDateTime_isoFormat() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T10:30:00", LocalDateTime.class, objectMapper);
        Assertions.assertInstanceOf(LocalDateTime.class, result);
        Assertions.assertEquals(LocalDateTime.of(2025, 1, 15, 10, 30, 0), result);
    }

    @Test
    @DisplayName("convertToDate - ISO 格式带毫秒字符串转 LocalDateTime")
    public void testStringToLocalDateTime_withMillis() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T10:30:00.123", LocalDateTime.class, objectMapper);
        Assertions.assertInstanceOf(LocalDateTime.class, result);
        LocalDateTime ldt = (LocalDateTime) result;
        Assertions.assertEquals(2025, ldt.getYear());
        Assertions.assertEquals(10, ldt.getHour());
        Assertions.assertEquals(30, ldt.getMinute());
    }

    @Test
    @DisplayName("convertToDate - 带时区字符串转 LocalDateTime，Jackson 不支持则原值返回")
    public void testStringToLocalDateTime_withTimezone_fallback() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T10:30:00+08:00", LocalDateTime.class, objectMapper);
        Assertions.assertEquals("2025-01-15T10:30:00+08:00", result);
    }

    @Test
    @DisplayName("convertToDate - UTC 字符串转 LocalDateTime（Jackson 丢弃时区信息）")
    public void testStringToLocalDateTime_utc() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T02:30:00Z", LocalDateTime.class, objectMapper);
        Assertions.assertInstanceOf(LocalDateTime.class, result);
        LocalDateTime ldt = (LocalDateTime) result;
        Assertions.assertEquals(2025, ldt.getYear());
        Assertions.assertEquals(2, ldt.getHour());
        Assertions.assertEquals(30, ldt.getMinute());
    }

    @Test
    @DisplayName("convertToDate - 空格分隔格式转 LocalDateTime，Jackson 不支持则原值返回")
    public void testStringToLocalDateTime_spaceFormat_fallback() {
        Object result = DateConvertUtil.convertToDate("2025-01-15 10:30:00", LocalDateTime.class, objectMapper);
        Assertions.assertEquals("2025-01-15 10:30:00", result);
    }

    @Test
    @DisplayName("convertToDate - 纯日期字符串转 LocalDateTime，Jackson 不支持则原值返回")
    public void testStringToLocalDateTime_dateOnly_fallback() {
        Object result = DateConvertUtil.convertToDate("2025-01-15", LocalDateTime.class, objectMapper);
        Assertions.assertEquals("2025-01-15", result);
    }

    // ======================== LocalDate ========================

    @Test
    @DisplayName("convertToDate - ISO 日期字符串转 LocalDate")
    public void testStringToLocalDate_isoFormat() {
        Object result = DateConvertUtil.convertToDate("2025-01-15", LocalDate.class, objectMapper);
        Assertions.assertInstanceOf(LocalDate.class, result);
        Assertions.assertEquals(LocalDate.of(2025, 1, 15), result);
    }

    @Test
    @DisplayName("convertToDate - ISO 日期时间字符串转 LocalDate（丢弃时间部分）")
    public void testStringToLocalDate_withTime() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T10:30:00", LocalDate.class, objectMapper);
        Assertions.assertInstanceOf(LocalDate.class, result);
        Assertions.assertEquals(LocalDate.of(2025, 1, 15), result);
    }

    @Test
    @DisplayName("convertToDate - 带时区字符串转 LocalDate，Jackson 不支持则原值返回")
    public void testStringToLocalDate_withTimezone_fallback() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T10:30:00+08:00", LocalDate.class, objectMapper);
        Assertions.assertEquals("2025-01-15T10:30:00+08:00", result);
    }

    @Test
    @DisplayName("convertToDate - 空格分隔格式转 LocalDate，Jackson 不支持则原值返回")
    public void testStringToLocalDate_spaceFormat_fallback() {
        Object result = DateConvertUtil.convertToDate("2025-01-15 10:30:00", LocalDate.class, objectMapper);
        Assertions.assertEquals("2025-01-15 10:30:00", result);
    }

    // ======================== LocalTime ========================

    @Test
    @DisplayName("convertToDate - ISO 时间字符串转 LocalTime")
    public void testStringToLocalTime_isoFormat() {
        Object result = DateConvertUtil.convertToDate("10:30:00", LocalTime.class, objectMapper);
        Assertions.assertInstanceOf(LocalTime.class, result);
        Assertions.assertEquals(LocalTime.of(10, 30, 0), result);
    }

    @Test
    @DisplayName("convertToDate - ISO 日期时间字符串转 LocalTime（丢弃日期部分）")
    public void testStringToLocalTime_withDateTime() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T10:30:00", LocalTime.class, objectMapper);
        Assertions.assertInstanceOf(LocalTime.class, result);
        Assertions.assertEquals(LocalTime.of(10, 30, 0), result);
    }

    @Test
    @DisplayName("convertToDate - 空格分隔格式转 LocalTime，Jackson 不支持则原值返回")
    public void testStringToLocalTime_spaceFormat_fallback() {
        Object result = DateConvertUtil.convertToDate("2025-01-15 10:30:00", LocalTime.class, objectMapper);
        Assertions.assertEquals("2025-01-15 10:30:00", result);
    }

    // ======================== OffsetDateTime ========================

    @Test
    @DisplayName("convertToDate - 带偏移量字符串转 OffsetDateTime，保留原始偏移量")
    public void testStringToOffsetDateTime_withOffset() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T10:30:00+08:00", OffsetDateTime.class, objectMapper);
        Assertions.assertInstanceOf(OffsetDateTime.class, result);
        OffsetDateTime odt = (OffsetDateTime) result;
        Assertions.assertEquals(2025, odt.getYear());
        Assertions.assertEquals(10, odt.getHour());
        Assertions.assertEquals(30, odt.getMinute());
        Assertions.assertEquals(ZoneOffset.ofHours(8), odt.getOffset());
    }

    @Test
    @DisplayName("convertToDate - UTC 字符串转 OffsetDateTime，保留 UTC 偏移量")
    public void testStringToOffsetDateTime_utc() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T02:30:00Z", OffsetDateTime.class, objectMapper);
        Assertions.assertInstanceOf(OffsetDateTime.class, result);
        OffsetDateTime odt = (OffsetDateTime) result;
        Assertions.assertEquals(ZoneOffset.UTC, odt.getOffset());
        Assertions.assertEquals(2, odt.getHour());
        Assertions.assertEquals(30, odt.getMinute());
    }

    @Test
    @DisplayName("convertToDate - 不带偏移量的 ISO 字符串转 OffsetDateTime，Jackson 不支持则原值返回")
    public void testStringToOffsetDateTime_noOffset_fallback() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T10:30:00", OffsetDateTime.class, objectMapper);
        Assertions.assertEquals("2025-01-15T10:30:00", result);
    }

    // ======================== ZonedDateTime ========================

    @Test
    @DisplayName("convertToDate - 带偏移量字符串转 ZonedDateTime，保留原始偏移量")
    public void testStringToZonedDateTime_withOffset() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T10:30:00+08:00", ZonedDateTime.class, objectMapper);
        Assertions.assertInstanceOf(ZonedDateTime.class, result);
        ZonedDateTime zdt = (ZonedDateTime) result;
        Assertions.assertEquals(2025, zdt.getYear());
        Assertions.assertEquals(10, zdt.getHour());
        Assertions.assertEquals(ZoneOffset.ofHours(8), zdt.getOffset());
    }

    @Test
    @DisplayName("convertToDate - UTC 字符串转 ZonedDateTime，保留 UTC 偏移量")
    public void testStringToZonedDateTime_utc() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T02:30:00Z", ZonedDateTime.class, objectMapper);
        Assertions.assertInstanceOf(ZonedDateTime.class, result);
        ZonedDateTime zdt = (ZonedDateTime) result;
        Assertions.assertEquals(ZoneOffset.UTC, zdt.getOffset());
        Assertions.assertEquals(2, zdt.getHour());
        Assertions.assertEquals(30, zdt.getMinute());
    }

    @Test
    @DisplayName("convertToDate - 不带偏移量的 ISO 字符串转 ZonedDateTime，Jackson 不支持则原值返回")
    public void testStringToZonedDateTime_noOffset_fallback() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T10:30:00", ZonedDateTime.class, objectMapper);
        Assertions.assertEquals("2025-01-15T10:30:00", result);
    }

    // ======================== Instant ========================

    @Test
    @DisplayName("convertToDate - UTC 字符串转 Instant")
    public void testStringToInstant_utc() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T02:30:00Z", Instant.class, objectMapper);
        Assertions.assertInstanceOf(Instant.class, result);
        Assertions.assertEquals(Instant.parse("2025-01-15T02:30:00Z"), result);
    }

    @Test
    @DisplayName("convertToDate - 带偏移量字符串转 Instant，Jackson 不支持则原值返回")
    public void testStringToInstant_withOffset_fallback() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T10:30:00+08:00", Instant.class, objectMapper);
        Assertions.assertEquals("2025-01-15T10:30:00+08:00", result);
    }

    @Test
    @DisplayName("convertToDate - 不带偏移量的 ISO 字符串转 Instant，Jackson 不支持则原值返回")
    public void testStringToInstant_noOffset_fallback() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T10:30:00", Instant.class, objectMapper);
        Assertions.assertEquals("2025-01-15T10:30:00", result);
    }

    // ======================== java.util.Date ========================

    @Test
    @DisplayName("convertToDate - ISO 格式字符串转 java.util.Date")
    public void testStringToUtilDate_isoFormat() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T10:30:00", java.util.Date.class, objectMapper);
        Assertions.assertInstanceOf(java.util.Date.class, result);
        java.util.Date date = (java.util.Date) result;
        Assertions.assertEquals(2025 - 1900, date.getYear());
        Assertions.assertEquals(0, date.getMonth());
        Assertions.assertEquals(15, date.getDate());
        Assertions.assertEquals(10, date.getHours());
        Assertions.assertEquals(30, date.getMinutes());
    }

    @Test
    @DisplayName("convertToDate - 纯日期字符串转 java.util.Date")
    public void testStringToUtilDate_dateOnly() {
        Object result = DateConvertUtil.convertToDate("2025-01-15", java.util.Date.class, objectMapper);
        Assertions.assertInstanceOf(java.util.Date.class, result);
    }

    @Test
    @DisplayName("convertToDate - 带时区字符串转 java.util.Date")
    public void testStringToUtilDate_withTimezone() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T10:30:00+08:00", java.util.Date.class, objectMapper);
        Assertions.assertInstanceOf(java.util.Date.class, result);
    }

    @Test
    @DisplayName("convertToDate - 空格分隔格式转 java.util.Date，Jackson 不支持则原值返回")
    public void testStringToUtilDate_spaceFormat_fallback() {
        Object result = DateConvertUtil.convertToDate("2025-01-15 10:30:00", java.util.Date.class, objectMapper);
        Assertions.assertEquals("2025-01-15 10:30:00", result);
    }

    // ======================== java.sql.Date ========================

    @Test
    @DisplayName("convertToDate - ISO 日期字符串转 java.sql.Date")
    public void testStringToSqlDate() {
        Object result = DateConvertUtil.convertToDate("2025-01-15", java.sql.Date.class, objectMapper);
        Assertions.assertInstanceOf(java.sql.Date.class, result);
        java.sql.Date date = (java.sql.Date) result;
        Assertions.assertEquals(2025 - 1900, date.getYear());
        Assertions.assertEquals(0, date.getMonth());
        Assertions.assertEquals(15, date.getDate());
    }

    // ======================== java.sql.Timestamp ========================

    @Test
    @DisplayName("convertToDate - ISO 格式字符串转 java.sql.Timestamp")
    public void testStringToSqlTimestamp() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T10:30:00", java.sql.Timestamp.class, objectMapper);
        Assertions.assertInstanceOf(java.sql.Timestamp.class, result);
        java.sql.Timestamp ts = (java.sql.Timestamp) result;
        Assertions.assertEquals(2025 - 1900, ts.getYear());
        Assertions.assertEquals(10, ts.getHours());
        Assertions.assertEquals(30, ts.getMinutes());
    }

    @Test
    @DisplayName("convertToDate - 空格分隔格式转 java.sql.Timestamp，Jackson 不支持则原值返回")
    public void testStringToSqlTimestamp_spaceFormat_fallback() {
        Object result = DateConvertUtil.convertToDate("2025-01-15 10:30:00", java.sql.Timestamp.class, objectMapper);
        Assertions.assertEquals("2025-01-15 10:30:00", result);
    }

    // ======================== java.sql.Time ========================

    @Test
    @DisplayName("convertToDate - ISO 格式字符串转 java.sql.Time，Jackson 不支持则原值返回")
    public void testStringToSqlTime_isoFormat_fallback() {
        Object result = DateConvertUtil.convertToDate("2025-01-15T10:30:00", java.sql.Time.class, objectMapper);
        Assertions.assertEquals("2025-01-15T10:30:00", result);
    }

    @Test
    @DisplayName("convertToDate - 时间格式字符串转 java.sql.Time")
    public void testStringToSqlTime_timeFormat() {
        Object result = DateConvertUtil.convertToDate("10:30:00", java.sql.Time.class, objectMapper);
        Assertions.assertInstanceOf(java.sql.Time.class, result);
        java.sql.Time time = (java.sql.Time) result;
        Assertions.assertEquals(10, time.getHours());
        Assertions.assertEquals(30, time.getMinutes());
    }

    @Test
    @DisplayName("convertToDate - 空格分隔格式转 java.sql.Time，Jackson 不支持则原值返回")
    public void testStringToSqlTime_spaceFormat_fallback() {
        Object result = DateConvertUtil.convertToDate("2025-01-15 10:30:00", java.sql.Time.class, objectMapper);
        Assertions.assertEquals("2025-01-15 10:30:00", result);
    }

    // ======================== Collection ========================

    @Test
    @DisplayName("convertToDate - Collection 中字符串批量转日期")
    public void testCollectionConversion() {
        List<String> dateStrings = Arrays.asList("2025-01-15", "2025-06-20", "2025-12-31");
        Object result = DateConvertUtil.convertToDate(dateStrings, LocalDate.class, objectMapper);
        Assertions.assertInstanceOf(List.class, result);
        @SuppressWarnings("unchecked")
        List<Object> converted = (List<Object>) result;
        Assertions.assertEquals(3, converted.size());
        Assertions.assertEquals(LocalDate.of(2025, 1, 15), converted.get(0));
        Assertions.assertEquals(LocalDate.of(2025, 6, 20), converted.get(1));
        Assertions.assertEquals(LocalDate.of(2025, 12, 31), converted.get(2));
    }

    @Test
    @DisplayName("convertToDate - Collection 中混合类型（字符串和非字符串）")
    public void testCollectionConversion_mixedTypes() {
        LocalDate existing = LocalDate.of(2025, 3, 1);
        List<Object> values = Arrays.asList("2025-01-15", existing);
        Object result = DateConvertUtil.convertToDate(values, LocalDate.class, objectMapper);
        Assertions.assertInstanceOf(List.class, result);
        @SuppressWarnings("unchecked")
        List<Object> converted = (List<Object>) result;
        Assertions.assertEquals(2, converted.size());
        Assertions.assertEquals(LocalDate.of(2025, 1, 15), converted.get(0));
        Assertions.assertSame(existing, converted.get(1));
    }
}