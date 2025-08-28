package io.github.pengxianggui.crud.join;

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

    public static <DTO> Object createMainInstance(DTO model) {
        DtoInfo dtoInfo = JoinWrapperUtil.getDtoInfo(model.getClass());
        Class entityClass = dtoInfo.getMainEntityClazz();
        try {
            return createEntityInstance(entityClass, model, dtoInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <DTO> List<Object> createJoinInstance(DTO model, Object mainEntity) {
        DtoInfo dtoInfo = JoinWrapperUtil.getDtoInfo(model.getClass());
        List<DtoInfo.JoinInfo> joinInfos = new ArrayList<>();
        joinInfos.addAll(dtoInfo.getInnerJoinInfo());
        joinInfos.addAll(dtoInfo.getLeftJoinInfo());
        joinInfos.addAll(dtoInfo.getRightJoinInfo());
        List<Object> entities = new ArrayList<>();
        try {
            for (DtoInfo.JoinInfo joinInfo : joinInfos) {
                Object entity = createEntityInstance(joinInfo.getJoinEntityClass(), model, dtoInfo);
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
    private static <T> T createEntityInstance(Class<T> entityClass, Object model, DtoInfo dtoInfo) throws InstantiationException, IllegalAccessException {
        List<DtoInfo.DtoField> fields = dtoInfo.getFields().stream()
                .filter(f -> f.isDbField() && !f.targetFieldNotExist() && !f.isJoinIgnoreForInsert())
                .filter(f -> Objects.equals(entityClass, f.getTargetClazz()))
                .collect(Collectors.toList());
        T entity = entityClass.newInstance();
        fields.forEach(field -> field.copyValue(model, entity));
        return entity;
    }
}
