package io.github.pengxianggui.crud.join;

import java.lang.annotation.*;

/**
 * @author pengxg
 * @date 2025/5/28 16:11
 */
@Repeatable(InnerJoins.class)
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
     * 目标类别名。当需要多次关联同一张表时，很可能会需要设置别名
     *
     * @return
     */
    String alias() default "";

    /**
     * join后的on条件
     *
     * @return
     */
    OnCond[] on();
}
