package io.github.pengxianggui.crud.join;

import java.lang.annotation.*;

/**
 * @author pengxg
 * @date 2025/5/28 16:11
 */
@Repeatable(RightJoins.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RightJoin {
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
