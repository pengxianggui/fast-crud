package io.github.pengxianggui.crud.batch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记实体字段不允许批量修改。
 * 批量修改默认放开实体的全部数据库字段（主键除外），审计字段、敏感字段等需要在这里排除；
 * 接口收到被标注的字段会直接拒绝，并把 {@link #value()} 作为原因返回。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreBatchUpdate {

    /**
     * 拒绝原因，会出现在接口报错和可用字段接口的 deniedFields 中
     */
    String value() default "该字段不允许批量修改";
}
