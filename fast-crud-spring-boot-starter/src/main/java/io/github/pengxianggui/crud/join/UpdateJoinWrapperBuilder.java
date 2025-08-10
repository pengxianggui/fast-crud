package io.github.pengxianggui.crud.join;

import cn.hutool.core.lang.Assert;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.UpdateJoinWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * 构建UpdateJoinWrapper实例
 *
 * @author pengxg
 * @date 2025/6/15 17:09
 */
@Slf4j
public class UpdateJoinWrapperBuilder<T, DTO> {
    private Class<T> mainClazz;
    private DtoInfo dtoInfo;
    private Consumer<UpdateJoinWrapper<T>> joinConsumer;
    private Consumer<UpdateJoinWrapper<T>> setConsumer;
    private Consumer<UpdateJoinWrapper<T>> whereConsumer;
    private boolean updateNull = true;

    /**
     * 构建UpdateJoinWrapper实例
     *
     * @param dtoClass dto类
     */
    public UpdateJoinWrapperBuilder(Class<DTO> dtoClass) {
        DtoInfo dtoInfo = JoinWrapperUtil.getDtoInfo(dtoClass);
        if (dtoInfo == null) {
            throw new ClassJoinParseException(dtoClass, "Can not found dtoInfo of entityClass:" + dtoClass.getName());
        }
        this.dtoInfo = dtoInfo;
        this.mainClazz = (Class<T>) dtoInfo.getMainEntityClazz();
        this.joinConsumer = w -> JoinWrapperUtil.addJoin(w, dtoInfo);
    }

    /**
     * 设置join
     *
     * @param customJoin 自定义的join信息
     * @return
     */
    public UpdateJoinWrapperBuilder<T, DTO> join(Consumer<UpdateJoinWrapper<T>> customJoin) {
        this.joinConsumer = customJoin;
        return this;
    }

    /**
     * 追加join
     *
     * @param customJoin 自定义的join信息
     * @return
     */
    public UpdateJoinWrapperBuilder<T, DTO> appendJoin(Consumer<UpdateJoinWrapper<T>> customJoin) {
        Consumer<UpdateJoinWrapper<T>> oldJoinConsumer = this.joinConsumer;
        this.joinConsumer = w -> {
            oldJoinConsumer.accept(w);
            customJoin.accept(w);
        };
        return this;
    }

    /**
     * 设置set
     *
     * @param model 根据model更新值
     * @return
     */
    public UpdateJoinWrapperBuilder<T, DTO> set(DTO model) {
        Assert.notNull(model, "model can not be null");
        Assert.equals(model.getClass(), dtoInfo.getDtoClazz(), "The class of model must be " + dtoInfo.getDtoClazz().getName());
        this.setConsumer = w -> JoinWrapperUtil.addSet(w, dtoInfo, model, updateNull);
        return this;
    }

    /**
     * 设置set
     *
     * @param customSet 自定义的set信息
     * @return
     */
    public UpdateJoinWrapperBuilder<T, DTO> set(Consumer<UpdateJoinWrapper<T>> customSet) {
        this.setConsumer = customSet;
        return this;
    }

    /**
     * 追加set
     *
     * @param customSet 自定义的set信息
     * @return
     */
    public UpdateJoinWrapperBuilder<T, DTO> appendSet(Consumer<UpdateJoinWrapper<T>> customSet) {
        Consumer<UpdateJoinWrapper<T>> oldSetConsumer = this.setConsumer;
        this.setConsumer = w -> {
            oldSetConsumer.accept(w);
            customSet.accept(w);
        };
        return this;
    }

    /**
     * 设置where条件
     *
     * @param customWhere 自定义的where信息
     * @return
     */
    public UpdateJoinWrapperBuilder<T, DTO> where(Consumer<UpdateJoinWrapper<T>> customWhere) {
        this.whereConsumer = customWhere;
        return this;
    }

    /**
     * 设置是否更新null字段
     *
     * @param updateNull 是否更新null字段
     * @return
     */
    public UpdateJoinWrapperBuilder<T, DTO> updateNull(boolean updateNull) {
        this.updateNull = updateNull;
        return this;
    }

    /**
     * 必须自定义set逻辑
     *
     * @return
     */
    public UpdateJoinWrapper<T> build() {
        UpdateJoinWrapper<T> wrapper = JoinWrappers.update(mainClazz);
        Assert.notNull(this.setConsumer, "setConsumer can not be null!");
        this.setConsumer.accept(wrapper);
        this.joinConsumer.accept(wrapper);
        Assert.notNull(this.whereConsumer, "whereConsumer can not be null!");
        this.whereConsumer.accept(wrapper);
        // 必须有where条件
        Assert.isTrue(wrapper.isNonEmptyOfWhere(), "Please specify the where statement!");
        return wrapper;
    }
}
