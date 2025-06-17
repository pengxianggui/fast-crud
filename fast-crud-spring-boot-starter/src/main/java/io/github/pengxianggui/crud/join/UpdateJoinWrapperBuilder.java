package io.github.pengxianggui.crud.join;

import cn.hutool.core.lang.Assert;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.UpdateJoinWrapper;
import io.github.pengxianggui.crud.query.Query;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;

import java.util.function.Consumer;

/**
 * 构建UpdateJoinWrapper实例
 *
 * @author pengxg
 * @date 2025/6/15 17:09
 */
public class UpdateJoinWrapperBuilder<T, D> {
    private Query query;
    private Class<T> mainClazz;
    private Class<D> dtoClazz;
    private Consumer<UpdateJoinWrapper<T>> customSet;
    private Consumer<UpdateJoinWrapper<T>> customJoin;
    private Consumer<UpdateJoinWrapper<T>> customWhere;

    /**
     * 将从dto类解析泛型({@link JoinMain})，若不匹配可能抛出异常
     *
     * @param query    查询条件
     * @param dtoClazz dto类
     */
    public UpdateJoinWrapperBuilder(Query query, Class<D> dtoClazz) {
        this(query, null, dtoClazz);
    }

    /**
     * 构建UpdateJoinWrapper实例
     *
     * @param query     查询条件
     * @param mainClazz 主类
     * @param dtoClazz  dto类
     */
    public UpdateJoinWrapperBuilder(Query query, Class<T> mainClazz, Class<D> dtoClazz) {
        this.query = query;
        this.mainClazz = mainClazz;
        this.dtoClazz = dtoClazz;
    }

    /**
     * 自定义set，会覆盖内置set组装
     *
     * @param customSet
     * @return
     */
    public UpdateJoinWrapperBuilder<T, D> set(Consumer<UpdateJoinWrapper<T>> customSet) {
        this.customSet = customSet;
        return this;
    }

    /**
     * 自定义join，会覆盖内置join组装
     *
     * @param customJoin
     * @return
     */
    public UpdateJoinWrapperBuilder<T, D> join(Consumer<UpdateJoinWrapper<T>> customJoin) {
        this.customJoin = customJoin;
        return this;
    }

    /**
     * 自定义where，会覆盖内置where组装
     *
     * @param customWhere
     * @return
     */
    public UpdateJoinWrapperBuilder<T, D> where(Consumer<UpdateJoinWrapper<T>> customWhere) {
        this.customWhere = customWhere;
        return this;
    }

    /**
     * 必须自定义set逻辑
     *
     * @return
     */
    public UpdateJoinWrapper<T> build() {
        Assert.notNull(this.customSet != null, "Please call set function to custom set!");
        return build((UpdateModelWrapper) null);
    }

    public UpdateJoinWrapper<T> build(D dto) {
        return build(new UpdateModelWrapper<>(dto));
    }

    public UpdateJoinWrapper<T> build(D dto, boolean updateNull) {
        return build(new UpdateModelWrapper<>(dto, updateNull));
    }

    private UpdateJoinWrapper<T> build(UpdateModelWrapper<D> dtoWrapper) {
        DtoInfo dtoInfo = JoinWrapperUtil.getDtoInfo(dtoClazz);
        if (dtoInfo == null) {
            throw new ClassJoinParseException(dtoClazz, "Can not found dtoInfo of dtoClass:" + dtoClazz.getName());
        }
        Class<T> mainClazz = this.mainClazz == null ? (Class<T>) dtoInfo.getMainEntityClazz() : this.mainClazz;
        UpdateJoinWrapper<T> wrapper = JoinWrappers.update(mainClazz);
        if (customSet != null) {
            customSet.accept(wrapper);
        } else {
            JoinWrapperUtil.addSet(wrapper, dtoInfo, dtoWrapper);
        }
        if (customJoin != null) {
            customJoin.accept(wrapper);
        } else {
            JoinWrapperUtil.addJoin(wrapper, dtoInfo);
        }
        if (customWhere != null) {
            customWhere.accept(wrapper);
        } else {
            JoinWrapperUtil.addConditions(wrapper, query.getConds(), dtoInfo);
        }
        return wrapper;
    }
}
