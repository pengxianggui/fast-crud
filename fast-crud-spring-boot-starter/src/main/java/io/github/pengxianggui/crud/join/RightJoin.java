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

    /**
     * 只读。表示join的类的字段只参与select查询，而不参与update、insert和delete(关联表的delete目前暂不支持内置实现，设计如此)。
     *
     * @return
     */
    boolean readonly() default true;
}
