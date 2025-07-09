package io.github.pengxianggui.crud.join;

import cn.hutool.core.lang.Assert;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.UpdateJoinWrapper;
import io.github.pengxianggui.crud.query.Cond;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
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
    private Consumer<UpdateJoinWrapper<T>> customJoin;
    private Consumer<UpdateJoinWrapper<T>> customSet;
    private Consumer<UpdateJoinWrapper<T>> customWhere;
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
        this.customJoin = w -> JoinWrapperUtil.addJoin(w, dtoInfo);
    }

    /**
     * 自定义join，会覆盖内置join组装
     *
     * @param customJoin
     * @return
     */
    public UpdateJoinWrapperBuilder<T, DTO> join(Consumer<UpdateJoinWrapper<T>> customJoin) {
        this.customJoin = customJoin;
        return this;
    }

    public UpdateJoinWrapperBuilder<T, DTO> set(Consumer<UpdateJoinWrapper<T>> setConsumer) {
        this.customSet = setConsumer;
        return this;
    }

    public UpdateJoinWrapperBuilder<T, DTO> set(DTO model) {
        Assert.notNull(model, "model can not be null");
        Assert.equals(model.getClass(), dtoInfo.getDtoClazz(), "The class of model must be " + dtoInfo.getDtoClazz().getName());
        this.customSet = w -> JoinWrapperUtil.addSet(w, dtoInfo, model, updateNull);
        return this;
    }

    /**
     * 自定义where，会覆盖内置where组装
     *
     * @param customWhere
     * @return
     */
    public UpdateJoinWrapperBuilder<T, DTO> where(Consumer<UpdateJoinWrapper<T>> customWhere) {
        this.customWhere = customWhere;
        return this;
    }

    public UpdateJoinWrapperBuilder<T, DTO> where(List<Cond> conditions) {
        this.customWhere = w -> JoinWrapperUtil.addConditions(w, conditions, dtoInfo);
        return this;
    }

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
        Assert.notNull(this.customSet, "Please specify the set statement!");
        this.customSet.accept(wrapper);
        Assert.notNull(this.customJoin, "Please specify the join statement!");
        this.customJoin.accept(wrapper);
        // 保险起见，必须有where条件
        Assert.isTrue(this.customWhere != null, "Please specify the where statement!");
        this.customWhere.accept(wrapper);
        return wrapper;
    }
}
