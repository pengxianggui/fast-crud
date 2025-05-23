package io.github.pengxianggui.crud.query.join;

import com.github.yulichang.toolkit.Constant;

/**
 * @author pengxg
 * @date 2025/5/23 08:34
 */
public enum JoinType {
    INNER(Constant.JOIN), LEFT(Constant.LEFT_JOIN), RIGHT(Constant.RIGHT_JOIN);

    private final String value;

    JoinType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
