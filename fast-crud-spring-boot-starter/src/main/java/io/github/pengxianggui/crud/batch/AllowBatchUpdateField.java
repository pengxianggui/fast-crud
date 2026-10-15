package io.github.pengxianggui.crud.batch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明服务端自动注入的审计列。
 * 批量修改不走「查实体再 updateById」的流程，MyBatis-Plus 的字段填充不会生效，
 * update_time 这类列需要在 batchUpdateFill() 里手动注入，并用本注解声明列名；
 * 未声明的列会被忽略，防止绕过字段白名单。
 * <pre>
 * &#64;AllowBatchUpdateField("update_time")
 * protected Map&lt;String, Object&gt; batchUpdateFill() {
 *     return Collections.singletonMap("update_time", LocalDateTime.now());
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowBatchUpdateField {

    /**
     * 允许服务端注入的数据库列名（蛇形，如 update_time）
     */
    String[] value();
}
