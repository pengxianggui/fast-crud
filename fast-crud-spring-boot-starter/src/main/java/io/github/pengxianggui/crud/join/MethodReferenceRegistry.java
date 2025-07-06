package io.github.pengxianggui.crud.join;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pengxg
 * @date 2025/5/29 11:56
 */
@Slf4j
public class MethodReferenceRegistry {
    private static final Map<String, SFunction<?, ?>> registry = new HashMap<>();

    public static <T, R> void register(String key, SFunction<T, R> function) {
        log.debug("Put a mapping from field to methodReference into registry! {} -> {}", key, function.toString());
        registry.put(key, function);
    }

    @SuppressWarnings("unchecked")
    public static <T, R> SFunction<T, R> getFunction(Class<?> clazz, Field field) {
        String key = getKey(clazz.getName(), field.getName());
        if (!registry.containsKey(key)) {
            throw new ClassJoinParseException(clazz, "No method reference found for " + key);
        }
        return (SFunction<T, R>) registry.get(key);
    }

    static String getKey(String className, String fieldName) {
        return className + "#" + fieldName;
    }
}
