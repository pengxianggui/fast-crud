package io.github.pengxianggui.crud.query.join;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定关联信息
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
     * 关联的entity类中的字段，不配置则默认为DTO类中的同名字段
     *
     * @return
     */
    String field() default "";
}
