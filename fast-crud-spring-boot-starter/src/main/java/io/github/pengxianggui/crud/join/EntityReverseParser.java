package io.github.pengxianggui.crud.join;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 实体类逆向解析。
 * 由于MPJ没有提供连表插入的API，所以只能基于DtoInfo解析主实体和关联实体，自行构造。
 *
 * @author pengxg
 * @date 2025/7/5 13:25
 */
public class EntityReverseParser {

    public static <DTO> Object createMainInstanceForInset(DTO model) {
        DtoInfo dtoInfo = JoinWrapperUtil.getDtoInfo(model.getClass());
        Class entityClass = dtoInfo.getMainEntityClazz();
        try {
            Object entity = createEntityInstanceForInsert(entityClass, model, dtoInfo);
            Assert.notNull(entity, "无法构造主类实例!请检查DTO类:{}", model.getClass().getName());
            return entity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <DTO> List<Object> createJoinInstanceForInsert(DTO model, Object mainEntity) {
        DtoInfo dtoInfo = JoinWrapperUtil.getDtoInfo(model.getClass());
        List<DtoInfo.JoinInfo> joinInfos = dtoInfo.getJoinInfos().stream().filter(j -> !j.isReadonly())
                .collect(Collectors.toList());
        List<Object> entities = new ArrayList<>();
        try {
            // FIXME 当存在别名时，即同一个表join了多次，会有问题
            for (DtoInfo.JoinInfo joinInfo : joinInfos) {
                // 只支持构造和主类直接关联的实体, 否则过于复杂, 暂不支持
                if (joinInfo.getTargetEntityClass() != mainEntity.getClass()) {
                    continue;
                }
                Object entity = createEntityInstanceForInsert(joinInfo.getJoinEntityClass(), model, dtoInfo);
                if (entity == null) {
                    continue;
                }
                entities.add(entity);
                // 回填关联字段
                for (DtoInfo.OnCondition condFieldRelate : joinInfo.getCondFieldRelates()) {
                    if (condFieldRelate.isConst()) {
                        continue;
                    }
                    Object value = ReflectUtil.getFieldValue(mainEntity, condFieldRelate.getTargetField());
                    ReflectUtil.setFieldValue(entity, condFieldRelate.getField(), value);
                }
            }
            return entities;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建entityClass类型对象
     *
     * @param entityClass 实体对象类型
     * @param model       实体对象值来源
     * @param dtoInfo     映射关系
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private static <T> T createEntityInstanceForInsert(Class<T> entityClass, Object model, DtoInfo dtoInfo) throws InstantiationException, IllegalAccessException {
        List<DtoInfo.DtoField> fields = dtoInfo.getFields().stream()
                .filter(f -> f.isDbField() && !f.targetFieldNotExist() && !f.ignoreForInsert())
                .filter(f -> Objects.equals(entityClass, f.getTargetClazz()))
                .collect(Collectors.toList());
        if (fields.isEmpty()) {
            return null;
        }
        T entity = entityClass.newInstance();
        fields.forEach(field -> field.copyValue(model, entity));
        return entity;
    }
}
