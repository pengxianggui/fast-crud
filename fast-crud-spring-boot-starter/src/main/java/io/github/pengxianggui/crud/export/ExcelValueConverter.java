package io.github.pengxianggui.crud.export;

/**
 * 导出值转换器。业务系统可注册 Spring Bean, 将特定Java类型转换为适合写入Excel单元格的值。
 * <p>
 * 转换器仅作用于数据导出, 不参与导入解析和模板导出。多个转换器按Spring的Ordered顺序排列,
 * 首个匹配字段声明类型的转换器生效。
 *
 * @param <T> 字段声明的Java类型
 * @author pengxg
 */
public interface ExcelValueConverter<T> {
    /**
     * 支持的字段Java类型。可使用接口类型以覆盖其所有实现类。
     */
    Class<T> supportedType();

    /**
     * 转换原始字段值。返回null表示导出空单元格。
     */
    Object convert(T value);

    /**
     * 判断是否支持字段的声明类型。
     */
    default boolean supports(Class<?> fieldType) {
        return fieldType != null && supportedType().isAssignableFrom(fieldType);
    }

    /**
     * 将Object值安全转换为声明类型后执行转换。
     */
    default Object convertValue(Object value) {
        return value == null ? null : convert(supportedType().cast(value));
    }
}
