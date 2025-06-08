package io.github.pengxianggui.crud.join;

/**
 * 类关联解析异常
 *
 * @author pengxg
 * @date 2025/5/23 14:31
 */
public class ClassJoinParseException extends RuntimeException {
    private Class<?> clazz;

    public ClassJoinParseException(Class<?> clazz, String message) {
        super(message);
        this.clazz = clazz;
    }
}
