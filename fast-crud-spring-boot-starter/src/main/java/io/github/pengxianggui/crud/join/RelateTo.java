package io.github.pengxianggui.crud.join;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段关联信息
 *
 * @author pengxg
 * @date 2025/5/22 18:41
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RelateTo {

    /**
     * 关联的entity类
     *
     * @return
     */
    Class<?> value();

    /**
     * 关联的entity类中的字段。存在三种情况:
     * <pre>
     * 1 当前注解修饰的字段类型是简单值类型时(详见{@link cn.hutool.core.util.ClassUtil#isSimpleValueType(Class)})，
     * 则此配置代表当前注解修饰字段关联的目标为{@link #value()}类中的字段名，若不配置，则默认为{@link #value()}中的同名字段。
     * 2 当前注解修饰的字段类型是集合时({@link java.util.Collection})会视为一对多查询, 但有两种具体情况:
     *  2.1 集合泛型是简单值类型: 则此项必须配置，表示将{@link #value()}中的对应字段值列表赋值给当前注解修饰的字段
     *  2.2 集合泛型是非简单值类型: 则此项无需配置，表示将{@link #value()}值列表按映射规则赋值为当前注解修饰的字段, 通常泛型类型往往就是{@link #value()}类或者对应的dto类
     * 3 当前注解修饰的字段类型是其它(往往是自定义类), 表示一对一映射, 此时无需配置此项, 通常字段类型就是{@link #value()}类或者对应的dto类
     * </pre>
     * 样例如下:
     * <pre>
     * // 1
     * &#064;RelateTo(value = OrderAddress.class, field="address") // 如果OrderAddress中的对应字段也是address, 则可省略
     * private String address;
     *
     * // 2.1
     * &#064;RelateTo(value = OrderItem.class, field = "id")
     * private List&lt;Long> orderItemIds;
     *
     * // 2.2
     * &#064;RelateTo(OrderItem.class)
     * private List&lt;OrderItem> orderItems;
     *
     * // 3
     * &#064;RelateTo(OrderAddress.class)
     * private OrderAddress address;
     * </pre>
     *
     * @return
     */
    String field() default "";

    /**
     * 是否是数据库字段。若标记为false，表示此注解修饰的字段不直接关联数据库字段
     *
     * @return
     */
    boolean dbField() default true;
}
