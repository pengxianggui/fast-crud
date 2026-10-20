package io.github.pengxianggui.crud.batch;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import io.github.pengxianggui.crud.util.ColumnUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 批量修改的字段白名单解析，本功能的安全边界。
 * 字段名由前端动态指定，如果不加限制就等于把 UPDATE 的 SET 子句交给客户端，所以：
 * 只认实体属性名（经 MyBatis-Plus 元数据解析成列名，解析不到直接拒绝）；
 * 默认放开全部业务字段（主键除外），用 {@link IgnoreBatchUpdate} 做减法排除审计、敏感字段。
 * 解析结果按「实体类 + 版本号」缓存，{@link #bumpVersion()} 可使缓存全部失效。
 */
@Slf4j
public class BatchUpdateFieldRegistry {

    private static final Map<CacheKey, Map<String, AllowedField>> CACHE = new ConcurrentHashMap<>();

    /**
     * 全局版本号。递增后缓存键随之变化，旧条目自然失效，无需遍历清理。
     */
    private static volatile long version = 0L;

    private BatchUpdateFieldRegistry() {
    }

    /**
     * 使已解析的白名单缓存全部失效。
     */
    public static void bumpVersion() {
        version++;
        log.debug("Batch update field registry cache invalidated, version={}", version);
    }

    /**
     * 获取实体可批量修改的字段，顺序为实体字段声明顺序。
     *
     * @return 字段名 -> 字段元数据；非 MyBatis-Plus 实体返回空 Map
     */
    public static Map<String, AllowedField> allowedFields(Class<?> entityClazz) {
        if (entityClazz == null) {
            return Collections.emptyMap();
        }
        CacheKey key = new CacheKey(entityClazz, version);
        Map<String, AllowedField> cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        Map<String, AllowedField> resolved = resolve(entityClazz);
        Map<String, AllowedField> existing = CACHE.putIfAbsent(key, resolved);
        return existing != null ? existing : resolved;
    }

    /**
     * 按属性名查找白名单中的字段。
     *
     * @return 命中返回字段元数据；字段不存在或被禁用返回 null
     */
    public static AllowedField find(Class<?> entityClazz, String property) {
        if (StrUtil.isBlank(property)) {
            return null;
        }
        String wrapped = ColumnUtil.wrapper(property);
        for (Map.Entry<String, AllowedField> entry : allowedFields(entityClazz).entrySet()) {
            if (Objects.equals(ColumnUtil.wrapper(entry.getKey()), wrapped)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 判断某属性是否允许批量修改。
     */
    public static boolean isAllowed(Class<?> entityClazz, String property) {
        return find(entityClazz, property) != null;
    }

    /**
     * 获取存在但被禁止批量修改的字段及原因，供前端置灰提示。
     *
     * @return 字段名 -> 拒绝原因
     */
    public static Map<String, String> deniedFields(Class<?> entityClazz) {
        Map<String, String> denied = new LinkedHashMap<>();
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClazz);
        if (tableInfo == null) {
            return denied;
        }
        if (StrUtil.isNotBlank(tableInfo.getKeyProperty())) {
            denied.put(tableInfo.getKeyProperty(), "主键不允许批量修改");
        }
        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
            Field field = ReflectUtil.getField(entityClazz, fieldInfo.getProperty());
            IgnoreBatchUpdate ignore = field == null ? null : field.getAnnotation(IgnoreBatchUpdate.class);
            if (ignore != null) {
                denied.put(fieldInfo.getProperty(), ignore.value());
            }
        }
        return denied;
    }

    private static Map<String, AllowedField> resolve(Class<?> entityClazz) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClazz);
        if (tableInfo == null) {
            log.warn("Can not resolve TableInfo for {}, batch update field whitelist is empty. "
                    + "Please make sure it is a MyBatis-Plus entity.", entityClazz.getName());
            return Collections.emptyMap();
        }

        Map<String, AllowedField> allowed = new LinkedHashMap<>();
        for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
            String property = fieldInfo.getProperty();
            Field field = ReflectUtil.getField(entityClazz, property);
            if (field != null && field.isAnnotationPresent(IgnoreBatchUpdate.class)) {
                log.debug("Field {}.{} is excluded from batch update by @IgnoreBatchUpdate",
                        entityClazz.getSimpleName(), property);
                continue;
            }
            BatchUpdatable mark = field == null ? null : field.getAnnotation(BatchUpdatable.class);
            String label = mark != null && StrUtil.isNotBlank(mark.value()) ? mark.value() : property;
            allowed.put(property, new AllowedField(property, fieldInfo.getColumn(),
                    fieldInfo.getPropertyType(), label));
        }
        log.debug("Resolved {} batch-updatable field(s) for {}: {}",
                allowed.size(), entityClazz.getSimpleName(), allowed.keySet());
        return allowed;
    }

    /**
     * 白名单中的一个可批量修改字段。
     */
    @Getter
    public static class AllowedField {

        /** 实体属性名（camelCase），接口入参使用的名字 */
        private final String property;

        /** 数据库列名，进入 UPDATE ... SET */
        private final String column;

        /** Java 属性类型，用于值校验与转换 */
        private final Class<?> propertyType;

        /** 展示名 */
        private final String label;

        AllowedField(String property, String column, Class<?> propertyType, String label) {
            this.property = property;
            this.column = column;
            this.propertyType = propertyType;
            this.label = label;
        }

        @Override
        public String toString() {
            return property + "(" + column + ")";
        }
    }

    /**
     * 批量修改可用字段清单，供前端渲染字段下拉。
     */
    public static List<Map<String, Object>> describe(Class<?> entityClazz) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AllowedField field : allowedFields(entityClazz).values()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("field", field.getProperty());
            item.put("label", field.getLabel());
            item.put("type", field.getPropertyType() == null ? null : field.getPropertyType().getSimpleName());
            list.add(item);
        }
        return list;
    }

    private static final class CacheKey {
        private final Class<?> entityClazz;
        private final long version;

        CacheKey(Class<?> entityClazz, long version) {
            this.entityClazz = entityClazz;
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CacheKey)) {
                return false;
            }
            CacheKey other = (CacheKey) o;
            return version == other.version && Objects.equals(entityClazz, other.entityClazz);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entityClazz, version);
        }
    }
}
