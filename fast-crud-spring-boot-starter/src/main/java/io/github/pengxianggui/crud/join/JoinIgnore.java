package io.github.pengxianggui.crud.join;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * join查询中忽略字段。标注后，查询结果和查询条件中都不生效
 *
 * @author pengxg
 * @date 2025/6/1 11:32
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinIgnore {
}
