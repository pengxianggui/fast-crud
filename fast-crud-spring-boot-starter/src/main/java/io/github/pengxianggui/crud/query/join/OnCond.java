package io.github.pengxianggui.crud.query.join;

import io.github.pengxianggui.crud.query.Opt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author pengxg
 * @date 2025/5/23 08:57
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OnCond {

    /**
     * 当前主类字段名称
     *
     * @return
     */
    String field() default "";

    /**
     * 操作符，默认为EQ(=)
     *
     * @return
     */
    Opt opt() default Opt.EQ;

    /**
     * 关联的entity类中的字段名称，不配置则默认为DTO类中的同名字段
     *
     * @return
     */
    String targetField() default "";
}
