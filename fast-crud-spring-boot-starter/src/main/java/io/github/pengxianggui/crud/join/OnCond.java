package io.github.pengxianggui.crud.join;

import io.github.pengxianggui.crud.query.Opt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 必须配合JoinMain和LeftJoin、InnerJoin或RightJoin使用。
 * 例如:
 * <pre>
 * &#064;JoinMain(A.class)
 * &#064;LeftJoin(value  = B.class, on = {&#064;OnCond(field  = "pid", targetField = "id")} )
 * </pre>
 * 则表示: select ... from A left join B on B.pid = A.id
 * <p>
 *
 * @author pengxg
 * @date 2025/5/23 08:57
 * @see LeftJoin
 * @see InnerJoin
 * @see RightJoin
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OnCond {

    /**
     * 当前join类的别名。当需要多次关联同一张表时，很可能会需要设置别名
     *
     * @return
     */
    String alias() default "";

    /**
     * 当前join的类中字段名称。
     *
     * @return
     */
    String field();

    /**
     * 操作符，默认为EQ(=)
     *
     * @return
     */
    Opt opt() default Opt.EQ;

    /**
     * 静态常量值。当此值不为空时, {@link #field()}、{@link #opt()}、{@link #constVal()}生效, 例如: `age > 30`, 此时{@link #targetClazz()}和{@link #targetField()}将无效
     *
     * @return
     */
    String constVal() default "";

    /**
     * 目标类别名, 当需要多次关联同一张表时, 很可能会需要设置别名
     *
     * @return
     */
    String targetAlias() default "";

    /**
     * 目标类, 如果不设置则默认为主表对应的entity类
     *
     * @return
     */
    Class<?> targetClazz() default Void.class;

    /**
     * 目标类中的字段名称, 不配置则默认和{@link #field()} 一致
     *
     * @return
     */
    String targetField() default "";
}
