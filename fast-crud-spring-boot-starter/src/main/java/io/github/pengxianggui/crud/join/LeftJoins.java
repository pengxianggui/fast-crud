package io.github.pengxianggui.crud.join;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * LeftJoin的容器注解, 请直接使用LeftJoin
 *
 * @author pengxg
 * @date 2025/7/9 10:27
 * @see LeftJoin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LeftJoins {
    LeftJoin[] value();
}
