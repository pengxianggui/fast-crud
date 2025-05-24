package io.github.pengxianggui.crud.query.join;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author pengxg
 * @date 2025/5/24 11:53
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinMain {

    Class<?> value() default void.class;
}
