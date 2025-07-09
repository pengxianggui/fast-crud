package io.github.pengxianggui.crud.join;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * InnerJoin的容器注解, 请直接使用InnerJoin
 *
 * @author pengxg
 * @date 2025/7/9 10:27
 * @see InnerJoin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InnerJoins {
    InnerJoin[] value();
}
