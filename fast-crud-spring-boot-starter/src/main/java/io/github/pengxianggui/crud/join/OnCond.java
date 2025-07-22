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
 * 注意: 暂不支持 on条件中带有静态值，如下:
 * <pre>
 * B.pid = 1
 * B.pid is not null
 * </pre>
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
     * 当前join的类中字段名称。
     *
     * @return
     */
    String field() default "";

    /**
     * 操作符，默认为EQ(=)
     *
     * @return
     */
    Opt opt() default Opt.EQ;

    /**
     * 目标类, 如果不设置则默认为主表对应的entity类
     *
     * @return
     */
    Class<?> targetClazz() default Void.class;

    /**
     * 主类中的字段名称, 不配置则默认和{@link #field()} 一致
     *
     * @return
     */
    String targetField() default "";
}
