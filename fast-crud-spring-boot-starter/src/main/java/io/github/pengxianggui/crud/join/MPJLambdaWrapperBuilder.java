package io.github.pengxianggui.crud.join;

import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.github.pengxianggui.crud.query.Query;

import java.util.function.Consumer;

/**
 * 构建MPJLambdaWrapper实例
 *
 * @author pengxg
 * @date 2025/5/23 16:04
 */
public class MPJLambdaWrapperBuilder<T> {
    private Query query;
    private Class<T> mainClazz;
    private Class<?> dtoClazz;
    private Consumer<MPJLambdaWrapper<T>> customSelect;
    private Consumer<MPJLambdaWrapper<T>> customJoin;
    private Consumer<MPJLambdaWrapper<T>> customWhere;
    private Consumer<MPJLambdaWrapper<T>> customOrder;

    /**
     * 将从dto类解析泛型({@link JoinMain})，若不匹配可能抛出异常
     *
     * @param query    查询条件
     * @param dtoClazz dto类
     */
    public MPJLambdaWrapperBuilder(Query query, Class<?> dtoClazz) {
        this(query, null, dtoClazz);
    }

    /**
     * 构建MPJLambdaWrapper实例
     *
     * @param query     查询条件
     * @param mainClazz 主表实体类
     * @param dtoClazz  dto类
     */
    public MPJLambdaWrapperBuilder(Query query, Class<T> mainClazz, Class<?> dtoClazz) {
        this.query = query;
        this.mainClazz = mainClazz;
        this.dtoClazz = dtoClazz;
    }

    /**
     * 自定义select，会覆盖内置select组装
     *
     * @param customSelect
     * @return
     */
    public MPJLambdaWrapperBuilder<T> select(Consumer<MPJLambdaWrapper<T>> customSelect) {
        this.customSelect = customSelect;
        return this;
    }

    /**
     * 自定义join，会覆盖内置join组装
     *
     * @param customJoin
     * @return
     */
    public MPJLambdaWrapperBuilder<T> join(Consumer<MPJLambdaWrapper<T>> customJoin) {
        this.customJoin = customJoin;
        return this;
    }

    /**
     * 自定义where，会覆盖内置where组装
     *
     * @param customWhere
     * @return
     */
    public MPJLambdaWrapperBuilder<T> where(Consumer<MPJLambdaWrapper<T>> customWhere) {
        this.customWhere = customWhere;
        return this;
    }

    /**
     * 自定义order，会覆盖内置order组装
     *
     * @param customOrder
     * @return
     */
    public MPJLambdaWrapperBuilder<T> order(Consumer<MPJLambdaWrapper<T>> customOrder) {
        this.customOrder = customOrder;
        return this;
    }

    public MPJLambdaWrapper<T> build() {
        DtoInfo dtoInfo = JoinWrapperUtil.getDtoInfo(dtoClazz);
        if (dtoInfo == null) {
            throw new ClassJoinParseException(dtoClazz, "Can not found dtoInfo of dtoClass:" + dtoClazz.getName());
        }
        Class<T> mainClazz = this.mainClazz == null ? (Class<T>) dtoInfo.getMainEntityClazz() : this.mainClazz;
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapper<>(mainClazz);
        if (customSelect != null) {
            customSelect.accept(wrapper);
        } else {
            JoinWrapperUtil.addSelect(wrapper, query.getCols(), query.isDistinct(), dtoInfo);
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
        if (customOrder != null) {
            customOrder.accept(wrapper);
        } else {
            JoinWrapperUtil.addOrders(wrapper, query.getOrders(), dtoInfo);
        }
        return wrapper;
    }
}
