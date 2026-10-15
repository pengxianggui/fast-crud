package io.github.pengxianggui.crud.batch;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import io.github.pengxianggui.crud.util.DateConvertUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 批量修改的字段值校验与类型转换。
 * 前端传来的 JSON 是弱类型的（数字可能是字符串、true 可能是 "true"），这里把值收敛到
 * 实体字段声明的类型上，做不到就报错，避免脏值写进数据库。
 */
public final class BatchUpdateValueConverter {

    private BatchUpdateValueConverter() {
    }

    /**
     * 把原始值转换为目标类型。
     *
     * @param raw    原始值
     * @param target 目标 Java 类型
     * @param field  字段名，仅用于报错信息
     * @return 转换后的值；raw 为 null 时返回 null（是否允许置空由 allowNull 决定）
     * @throws IllegalArgumentException 值无法转换为目标类型
     */
    public static Object convert(Object raw, Class<?> target, String field) {
        if (target == null || target == Object.class) {
            return raw;
        }
        if (raw == null) {
            // 是否允许置空由 Service 层依据 allowNull 判定
            return null;
        }

        if (target.isInstance(raw) && !(raw instanceof Collection)) {
            return raw;
        }

        if (target.isEnum()) {
            return convertEnum(raw, target, field);
        }
        if (DateConvertUtil.isDateType(target)) {
            return convertDate(raw, target, field);
        }
        if (target == Boolean.class || target == boolean.class) {
            return toBoolean(raw, field);
        }
        if (isNumeric(target)) {
            return convertNumber(raw, target, field);
        }
        if (target == String.class) {
            return Convert.toStr(raw);
        }
        if (target == Character.class || target == char.class) {
            String str = Convert.toStr(raw);
            if (str == null || str.length() != 1) {
                throw new IllegalArgumentException("字段[" + field + "]期望单个字符，实际为: " + str);
            }
            return str.charAt(0);
        }
        if (Collection.class.isAssignableFrom(target)) {
            // 集合类型（如 List<FileItem>）整体透传：其内部结构由具体 TypeHandler 负责序列化
            return raw instanceof Collection ? new ArrayList<>((Collection<?>) raw) : raw;
        }
        if (target.isArray()) {
            return convertArray(raw, target, field);
        }
        // 其余复杂类型：交由 Hutool 尝试，失败则报错
        try {
            return Convert.convert(target, raw);
        } catch (Exception e) {
            throw new IllegalArgumentException("字段[" + field + "]的值无法转换为 " + target.getSimpleName()
                    + "，实际值: " + raw);
        }
    }

    /**
     * 日期时间值收敛。
     * 优先用 {@link DateConvertUtil#convertToDate(Object, Class)}，它基于 Spring 上下文里的
     * Jackson ObjectMapper，能尊重使用方配置的格式与时区。
     * 但该方法在无 Spring 容器时会抛异常，而类型校验不该依赖容器才能工作，故退化为 Hutool 解析。
     */
    private static Object convertDate(Object raw, Class<?> target, String field) {
        if (!(raw instanceof CharSequence)) {
            return raw;
        }
        try {
            Object converted = DateConvertUtil.convertToDate(raw, target);
            if (converted != null && !(converted instanceof CharSequence)) {
                return converted;
            }
        } catch (Exception ignored) {
            // 无 Spring 上下文，走下面的兜底
        }
        return convertDateByHutool(Convert.toStr(raw), target, field);
    }

    private static Object convertDateByHutool(String text, Class<?> target, String field) {
        // Hutool 的 DateUtil.parse 不认 ISO-8601 的 'T' 分隔符
        String normalized = text.trim().replace('T', ' ');
        try {
            java.util.Date parsed = DateUtil.parse(normalized);
            Object converted = Convert.convert(target, parsed);
            if (converted != null) {
                return converted;
            }
        } catch (Exception ignored) {
            // 落到下面的报错
        }
        throw new IllegalArgumentException("字段[" + field + "]的值[" + text + "]无法解析为 "
                + target.getSimpleName() + "，建议使用 yyyy-MM-dd / yyyy-MM-dd HH:mm:ss / HH:mm:ss 格式");
    }

    private static Object convertEnum(Object raw, Class<?> target, String field) {
        String name = Convert.toStr(raw);
        if (StrUtil.isBlank(name)) {
            throw new IllegalArgumentException("字段[" + field + "]期望枚举 " + target.getSimpleName() + "，实际为空值");
        }
        for (Object constant : target.getEnumConstants()) {
            if (((Enum<?>) constant).name().equalsIgnoreCase(name.trim())) {
                return constant;
            }
        }
        throw new IllegalArgumentException("字段[" + field + "]的值[" + name + "]不是合法枚举，可选值: "
                + java.util.Arrays.toString(target.getEnumConstants()));
    }

    private static Boolean toBoolean(Object raw, String field) {
        if (raw instanceof Boolean) {
            return (Boolean) raw;
        }
        String str = Convert.toStr(raw);
        if ("true".equalsIgnoreCase(str) || "1".equals(str) || "y".equalsIgnoreCase(str)
                || "yes".equalsIgnoreCase(str)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(str) || "0".equals(str) || "n".equalsIgnoreCase(str)
                || "no".equalsIgnoreCase(str)) {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("字段[" + field + "]的值[" + str + "]无法识别为布尔值");
    }

    private static Object convertNumber(Object raw, Class<?> target, String field) {
        try {
            return Convert.convert(target, raw);
        } catch (Exception e) {
            throw new IllegalArgumentException("字段[" + field + "]的值[" + raw + "]无法转换为 "
                    + target.getSimpleName());
        }
    }

    private static Object convertArray(Object raw, Class<?> target, String field) {
        Class<?> componentType = target.getComponentType();
        List<?> items;
        if (raw instanceof Collection) {
            items = new ArrayList<>((Collection<?>) raw);
        } else if (raw.getClass().isArray()) {
            int len = Array.getLength(raw);
            List<Object> list = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                list.add(Array.get(raw, i));
            }
            items = list;
        } else {
            throw new IllegalArgumentException("字段[" + field + "]期望数组，实际值: " + raw);
        }
        Object array = Array.newInstance(componentType, items.size());
        for (int i = 0; i < items.size(); i++) {
            Array.set(array, i, convert(items.get(i), componentType, field + "[" + i + "]"));
        }
        return array;
    }

    private static boolean isNumeric(Class<?> type) {
        return Number.class.isAssignableFrom(type)
                || type == int.class || type == long.class || type == short.class
                || type == byte.class || type == double.class || type == float.class;
    }

    /**
     * 判断是否为集合、数组或复杂对象，这类字段不适合在批量修改弹窗里直接输入。
     * 两个刻意的处理：type 为 null 时返回 true（拿不到类型就过滤掉，不放行）；
     * 显式判定 CharSequence，因为 Hutool 的 ClassUtil.isBasicType(String.class) 返回 false，
     * 只依赖它会把最常用的 String 字段误判为复杂类型。
     */
    public static boolean isComplexType(Class<?> type) {
        if (type == null) {
            return true;
        }
        if (Collection.class.isAssignableFrom(type) || type.isArray()) {
            return true;
        }
        return !CharSequence.class.isAssignableFrom(type)
                && !ClassUtil.isBasicType(type)
                && !type.isEnum()
                && !DateConvertUtil.isDateType(type);
    }
}
