package io.github.pengxianggui.crud.join;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RightJoin的容器注解, 请直接使用RightJoin
 *
 * @author pengxg
 * @date 2025/7/9 10:27
 * @see RightJoin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RightJoins {
    RightJoin[] value();
}
