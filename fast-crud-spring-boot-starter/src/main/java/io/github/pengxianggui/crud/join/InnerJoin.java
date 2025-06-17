package io.github.pengxianggui.crud.join;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author pengxg
 * @date 2025/5/28 16:11
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface InnerJoin {
    /**
     * 关联目标的entity类
     *
     * @return
     */
    Class<?> value();

    /**
     * join后的on条件
     *
     * @return
     */
    OnCond[] on();
}
