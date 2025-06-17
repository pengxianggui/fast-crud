package io.github.pengxianggui.crud.join;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * 类关联解析异常
 *
 * @author pengxg
 * @date 2025/5/23 14:31
 */
public class ClassJoinParseException extends RuntimeException {
    @Getter
    private Class<?> clazz;

    public ClassJoinParseException(Class<?> clazz, String messageTmpl, Object... args) {
        this(clazz, StrUtil.format(messageTmpl, args));
    }

    public ClassJoinParseException(Class<?> clazz, String message) {
        super(message);
        this.clazz = clazz;
    }
}
