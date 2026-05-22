package io.github.pengxianggui.crud.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.time.temporal.Temporal;
import java.util.*;

@Slf4j
public class DateConvertUtil {

    private static volatile ObjectMapper objectMapper;

    public static boolean isDateType(Class<?> type) {
        return Temporal.class.isAssignableFrom(type)
                || java.util.Date.class.isAssignableFrom(type)
                || java.util.Calendar.class.isAssignableFrom(type);
    }

    /**
     *
     * @param val
     * @param propertyType
     * @return
     */
    public static Object convertToDate(Object val, Class<?> propertyType) {
        ObjectMapper mapper = getObjectMapperFromSpringContext();
        Assert.notNull(mapper, "Can't find spring bean: {}, please check your configuration", ObjectMapper.class);
        return convertToDate(val, propertyType, mapper);
    }

    /**
     * 转换任意值为指定类型的日期对象。若val不为
     *
     * @param val
     * @param propertyType
     * @param objectMapper
     * @return
     */
    public static Object convertToDate(Object val, Class<?> propertyType, ObjectMapper objectMapper) {
        if (!isDateType(propertyType)) {
            return val;
        }
        if (val instanceof String) {
            String dateStr = ((String) val).trim();
            if (StrUtil.isBlank(dateStr)) {
                return val;
            }
            return convertDateString(dateStr, propertyType, objectMapper);
        }
        if (val instanceof Collection) {
            Collection<?> collection = (Collection<?>) val;
            List<Object> converted = new ArrayList<>(collection.size());
            for (Object item : collection) {
                if (item instanceof String) {
                    converted.add(convertDateString(((String) item).trim(), propertyType, objectMapper));
                } else {
                    converted.add(item);
                }
            }
            return converted;
        }
        return val;
    }

    private static Object convertDateString(String dateStr, Class<?> targetDateType, ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "objectMapper is not null");
        try {
            return objectMapper.convertValue(dateStr, objectMapper.constructType(targetDateType));
        } catch (Exception e) {
            log.error("Failed to convert dateStr as {}, return original value:{}, check your jackson configuration", targetDateType, dateStr);
            return dateStr;
        }
    }

    /**
     * 通过hutool {@link Convert}转换，不能很好处理时区问题，可能导致时间偏移，建议使用{@link #convertToDate(Object, Class)}
     *
     * @param dateStr
     * @param targetDateType
     * @return
     */
    private static Object convertToDateByHutool(String dateStr, Class<?> targetDateType) {
        try {
            java.util.Date date = DateUtil.parse(dateStr);
            if (targetDateType == java.sql.Date.class) {
                return new java.sql.Date(date.getTime());
            } else if (targetDateType == java.sql.Timestamp.class) {
                return new java.sql.Timestamp(date.getTime());
            } else if (targetDateType == java.sql.Time.class) {
                return new java.sql.Time(date.getTime());
            }
            return Convert.convert(targetDateType, date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private static ObjectMapper getObjectMapperFromSpringContext() {
        if (objectMapper == null) {
            synchronized (DateConvertUtil.class) {
                if (objectMapper == null) {
                    try {
                        objectMapper = SpringUtil.getBean(ObjectMapper.class);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return objectMapper;
    }
}