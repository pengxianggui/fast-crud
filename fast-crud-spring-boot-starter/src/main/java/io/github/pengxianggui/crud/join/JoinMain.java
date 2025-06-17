package io.github.pengxianggui.crud.join;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明主表对应的entity
 *
 * @author pengxg
 * @date 2025/5/24 11:53
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinMain {

    /**
     * 主表对应的entity类
     *
     * @return
     */
    Class<?> value();
}
