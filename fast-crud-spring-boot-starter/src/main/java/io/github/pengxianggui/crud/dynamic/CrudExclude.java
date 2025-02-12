package io.github.pengxianggui.crud.dynamic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 从父类继承的接口中，需要排除的CRUD接口, 声明排除的接口会动态剔除，接口文档和接口都不会存在
 * @author pengxg
 * @date 2025/2/7 21:40
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CrudExclude {
    CrudMethod[] value() default {};
}
