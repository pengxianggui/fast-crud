package io.github.pengxianggui.crud.join;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.UpdateJoinWrapper;
import io.github.pengxianggui.crud.query.Cond;

import java.util.List;
import java.util.function.Consumer;

/**
 * 构建UpdateJoinWrapper实例
 *
 * @author pengxg
 * @date 2025/6/15 17:09
 */
public class UpdateJoinWrapperBuilder<T> {
    private Class<T> mainClazz;
    private Class<?> dtoClazz;
    private DtoInfo dtoInfo;
    private List<Cond> conditions;
    private Consumer<UpdateJoinWrapper<T>> customJoin;
    private Consumer<UpdateJoinWrapper<T>> customWhere;
    private boolean updateNull = true;

    /**
     * 构建UpdateJoinWrapper实例
     *
     * @param entityClass dto类
     */
    public UpdateJoinWrapperBuilder(Class<T> entityClass, Class<?> dtoClass) {
        this.mainClazz = entityClass;
        this.dtoClazz = dtoClass;
        DtoInfo dtoInfo = JoinWrapperUtil.getDtoInfo(dtoClass);
        if (dtoInfo == null) {
            throw new ClassJoinParseException(entityClass, "Can not found dtoInfo of entityClass:" + entityClass.getName());
        }
        this.dtoInfo = dtoInfo;
        Assert.equals(dtoInfo.getMainEntityClazz(), entityClass,
                "The main type is inconsistent with the main type declared in the dto");
    }

    /**
     * 自定义join，会覆盖内置join组装
     *
     * @param customJoin
     * @return
     */
    public UpdateJoinWrapperBuilder<T> join(Consumer<UpdateJoinWrapper<T>> customJoin) {
        this.customJoin = customJoin;
        return this;
    }

    /**
     * 自定义where，会覆盖内置where组装
     *
     * @param customWhere
     * @return
     */
    public UpdateJoinWrapperBuilder<T> where(Consumer<UpdateJoinWrapper<T>> customWhere) {
        this.customWhere = customWhere;
        return this;
    }

    public UpdateJoinWrapperBuilder<T> where(List<Cond> conditions) {
        this.conditions = conditions;
        return this;
    }

    public UpdateJoinWrapperBuilder<T> updateNull(boolean updateNull) {
        this.updateNull = updateNull;
        return this;
    }

    /**
     * 必须自定义set逻辑
     *
     * @return
     */
    public UpdateJoinWrapper<T> build(Consumer<UpdateJoinWrapper<T>> setConsumer) {
        UpdateJoinWrapper<T> wrapper = JoinWrappers.update(mainClazz);
        setConsumer.accept(wrapper);
        buildWrapper(wrapper);
        return wrapper;
    }

    public UpdateJoinWrapper<T> build(Object dto) {
        UpdateJoinWrapper<T> wrapper = JoinWrappers.update(mainClazz);
        JoinWrapperUtil.addSet(wrapper, dtoInfo, dto, updateNull);
        buildWrapper(wrapper);
        return wrapper;
    }

    private void buildWrapper(UpdateJoinWrapper<T> wrapper) {
        if (customJoin != null) {
            customJoin.accept(wrapper);
        } else {
            JoinWrapperUtil.addJoin(wrapper, dtoInfo);
        }
        if (customWhere != null) {
            customWhere.accept(wrapper);
        }
        if (CollectionUtil.isNotEmpty(conditions)) {
            JoinWrapperUtil.addConditions(wrapper, conditions, dtoInfo);
        }
    }
}
