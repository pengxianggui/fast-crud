package io.github.pengxianggui.crud.query.join;

import java.lang.annotation.*;

/**
 * @author pengxg
 * @date 2025/5/23 08:29
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinTo {

//    /**
//     * 关联来源的Entity类，默认是当前类。暂不启用，在mybatis-plus-join官方文档中没有看到 A join B 再 B join C的样例，因此暂不支持
//     *
//     * @return
//     */
//    Class<?> from() default void.class;

    /**
     * 连表类型(默认是LEFT
     *
     * @return
     */
    JoinType joinType() default JoinType.LEFT;

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
