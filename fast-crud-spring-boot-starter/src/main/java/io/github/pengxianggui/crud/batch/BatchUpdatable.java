package io.github.pengxianggui.crud.batch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 显式声明实体字段允许批量修改，并指定展示名。
 * 字段默认就是允许的，通常无需使用本注解，主要用于给字段起一个中文名供前端下拉展示。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BatchUpdatable {

    /**
     * 字段展示名，留空则取实体字段名
     */
    String value() default "";
}
