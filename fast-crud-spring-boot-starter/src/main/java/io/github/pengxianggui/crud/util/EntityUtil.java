package io.github.pengxianggui.crud.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;

import java.beans.Transient;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * entity工具类, 获取一些基于mybatisplus的信息
 *
 * @author pengxg
 * @date 2024-12-03 9:54
 */
public class EntityUtil {

    /**
     * 获取类主键属性名称，注意是类属性名，而不是映射的数据库字段名
     *
     * @return
     */
    public static <T> String getPkName(T entity) {
        Assert.notNull(entity, "entity值为null, 无法获取主键名");
        return getPkName(entity.getClass());
    }

    /**
     * 获取类主键属性名称，注意是类属性名，而不是映射的数据库字段名
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> String getPkName(Class<T> clazz) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(clazz);
        Assert.notNull(tableInfo, "无法获取entity的tableInfo,请确保entity是一个映射有数据库表的类: " + clazz.getName());
        if (StrUtil.isNotBlank(tableInfo.getKeyProperty())) {
            return tableInfo.getKeyProperty();
        }
        return null;
    }

    /**
     * 获取主键值
     *
     * @param entity
     * @return
     */
    public static <T> Serializable getPkVal(T entity) {
        Assert.notNull(entity, "entity值为null, 无法获取主键值");
        String pkName = getPkName(entity);
        Assert.notNull(pkName, "无法通过主键名获取主键值, 主键名为空");
        return (Serializable) ReflectUtil.getFieldValue(entity, pkName);
    }


    /**
     * 获取主键值, 指定entity类
     *
     * @param obj   通常是dto类
     * @param clazz entity类
     * @return
     */
    public static <T> Serializable getPkVal(Object obj, Class<T> clazz) {
        Assert.notNull(obj, "entity值为null, 无法获取主键值");
        String pkName = getPkName(clazz);
        Assert.notNull(pkName, "无法通过主键名获取主键值, 主键名为空");
        return (Serializable) ReflectUtil.getFieldValue(obj, pkName);
    }

    /**
     * 判断是否声明为非数据库字段。满足以下条件返回 true
     * <pre>
     * 1. static、transient修饰
     * 2. @Transient修饰
     * 3. @TableField修饰，且注解中的exist属性为false
     * </pre>
     * 注意: 若返回false只能表示未声明字段为非数据库字段，而不能表示字段就是数据库字段！
     *
     * @param field
     * @return
     */
    public static boolean isMarkAsNotDbField(Field field) {
        if (Modifier.isStatic(field.getModifiers())
                || Modifier.isTransient(field.getModifiers())) { // 排除static和transient
            return true;
        }
        if (field.isAnnotationPresent(Transient.class)) { // 排除@Transient
            return true;
        }
        if (field.isAnnotationPresent(TableField.class)
                && field.getAnnotation(TableField.class).exist() == Boolean.FALSE) { // 排除mybatis声明的非数据库字段
            return true;
        }
        return false;
    }

    /**
     * 获取主键数据库里字段名
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> String getDbPkName(Class<T> clazz) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(clazz);
        Assert.notNull(tableInfo, "无法获取entity的tableInfo,请确保entity是一个映射有数据库表的类: " + clazz.getName());
        if (StrUtil.isNotBlank(tableInfo.getKeyColumn())) {
            return tableInfo.getKeyColumn();
        }
        return null;
    }

    /**
     * 获取数据库字段名称
     *
     * @param entity    实体类，必须是一个映射表的entity
     * @param fieldName entity类字段名
     * @return
     */
    public static <T> String getDbFieldName(T entity, String fieldName) {
        Assert.notNull(entity, "entity值为null, 无法获取属性(%s)映射的数据库字段名", fieldName);
        return getDbFieldName(entity.getClass(), fieldName);
    }

    public static String getDbFieldName(Class clazz, String fieldName) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(clazz);
        Assert.notNull(tableInfo, "无法获取entity的tableInfo,请确保entity是一个映射有数据库表的类: " + clazz.getName());
        String wrappedFieldName = ColumnUtil.wrapper(fieldName);
        if (wrappedFieldName.equals(ColumnUtil.wrapper(tableInfo.getKeyProperty()))) {
            return tableInfo.getKeyColumn();
        }

        // 通过字段名获取映射的数据库字段名
        return tableInfo.getFieldList().stream()
                .filter(meta -> Objects.equals(ColumnUtil.wrapper(meta.getProperty()), wrappedFieldName)) // 防止后者被`处理
                .map(meta -> meta.getColumn())
                .findFirst()
                .orElse(null);
    }

    /**
     * 判断字段是否需要更新，通过mybatisplus的@TableField 更新策略、结合入参updateNull联合判断，注意: @TableField更新策略的优先级更高。
     *
     * @param field      Entity字段
     * @param fieldValue 字段值
     * @param updateNull 是否更新null值，优先级低于@TableField注解中的更新策略
     * @return 若需要更新，则返回true
     */
    public static boolean fieldNeedUpdate(Field field, Object fieldValue, boolean updateNull) {
        boolean defaultPredicate = (fieldValue != null || updateNull); // 默认判断条件
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField == null) { // 无@TableField修饰，则判断依据交给入参updateNull
            return defaultPredicate;
        }
        boolean exist = tableField.exist();
        if (exist == Boolean.FALSE) { // 针对不存在的字段, 直接返回false
            return false;
        }
        // 其它情况由更新策略和入参updateNull组合判断，更新策略优先级高于入参updateNull
        FieldStrategy updateStrategy = tableField.updateStrategy();
        switch (updateStrategy) {
            case IGNORED:
            case ALWAYS:
                return true;
            case NOT_NULL:
                return fieldValue != null;
            case NOT_EMPTY:
                return (fieldValue instanceof CharSequence) ? StrUtil.isNotBlank((CharSequence) fieldValue) : fieldValue != null;
            case NEVER:
                return false;
            case DEFAULT:
            default:
                return defaultPredicate;
        }
    }

}
