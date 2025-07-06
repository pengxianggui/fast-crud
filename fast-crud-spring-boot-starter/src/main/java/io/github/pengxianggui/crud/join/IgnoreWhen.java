package io.github.pengxianggui.crud.join;

/**
 * @author pengxg
 * @date 2025/6/15 21:41
 */
public enum IgnoreWhen {
    /**
     * 查询时忽略。包括select、where、order都将忽略
     */
    Query,
    /**
     * 插入时忽略。
     */
    Insert,
    /**
     * 更新时忽略。包括set、where都将忽略
     */
    Update
}
