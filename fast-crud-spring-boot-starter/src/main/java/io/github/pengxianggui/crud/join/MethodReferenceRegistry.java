package io.github.pengxianggui.crud.join;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pengxg
 * @date 2025/5/29 11:56
 */
public class MethodReferenceRegistry {
    private static final Map<String, SFunction<?, ?>> registry = new HashMap<>();

    public static <T, R> void register(String key, SFunction<T, R> function) {
        registry.put(key, function);
    }

    @SuppressWarnings("unchecked")
    public static <T, R> SFunction<T, R> getFunction(Field field) {
        String key = getKey(field.getDeclaringClass().getName(), field.getName());
        if (!registry.containsKey(key)) {
            throw new ClassJoinParseException(field.getDeclaringClass(), "No method reference found for " + field.getName());
        }
        return (SFunction<T, R>) registry.get(key);
    }

    static String getKey(String className, String fieldName) {
        return className + "#" + fieldName;
    }
}
