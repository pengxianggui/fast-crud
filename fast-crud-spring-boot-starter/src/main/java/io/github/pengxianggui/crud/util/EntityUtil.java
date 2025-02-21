package io.github.pengxianggui.crud.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;

import java.io.Serializable;
import java.util.Objects;

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
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entity.getClass());
        Assert.notNull(tableInfo, "无法获取entity的tableInfo,请确保entity是一个映射有数据库表的类: " + entity.getClass().getName());
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
}
