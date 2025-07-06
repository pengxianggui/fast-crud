package io.github.pengxianggui.crud.join;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * join查询、更新中忽略字段
 *
 * @author pengxg
 * @date 2025/6/1 11:32
 * @see IgnoreWhen
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinIgnore {
    
    /**
     * 指定何时忽略，默认所有场景
     *
     * @return
     */
    IgnoreWhen[] value() default {IgnoreWhen.Query, IgnoreWhen.Insert, IgnoreWhen.Update};
}
