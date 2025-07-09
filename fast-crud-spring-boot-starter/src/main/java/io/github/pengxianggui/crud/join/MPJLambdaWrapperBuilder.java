package io.github.pengxianggui.crud.join;

import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.github.pengxianggui.crud.query.Cond;
import io.github.pengxianggui.crud.query.Order;

import java.util.List;
import java.util.function.Consumer;

/**
 * 基于定义的DTO类，构建MPJLambdaWrapper实例
 *
 * @author pengxg
 * @date 2025/5/23 16:04
 */
public class MPJLambdaWrapperBuilder<T> {
    private final Class<T> mainClazz;
    private final DtoInfo dtoInfo;
    private Consumer<MPJLambdaWrapper<T>> customSelect;
    private Consumer<MPJLambdaWrapper<T>> customJoin;
    private Consumer<MPJLambdaWrapper<T>> customWhere;
    private Consumer<MPJLambdaWrapper<T>> customOrder;

    /**
     * 将从dto类解析构造MPJLambdaWrapper。
     * <p>
     * 默认select为dtoClazz中所有符合条件的字段; 默认join信息为dtoClazz中定义的join信息
     *
     * @param dtoClazz dto类
     */
    public MPJLambdaWrapperBuilder(Class<?> dtoClazz) {
        DtoInfo dtoInfo = JoinWrapperUtil.getDtoInfo(dtoClazz);
        if (dtoInfo == null) {
            throw new ClassJoinParseException(dtoClazz, "Can not found dtoInfo of dtoClass:" + dtoClazz.getName());
        }
        this.dtoInfo = dtoInfo;
        this.mainClazz = (Class<T>) dtoInfo.getMainEntityClazz();
        this.customSelect = w -> JoinWrapperUtil.addSelect(w, null, null, dtoInfo); // 默认查dto中所有(符合条件的)字段
        this.customJoin = w -> JoinWrapperUtil.addJoin(w, dtoInfo); // 默认从dto解析join信息
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
     * 自定义select，会覆盖内置select组装
     *
     * @param cols
     * @return
     */
    public MPJLambdaWrapperBuilder<T> select(List<String> cols) {
        this.customSelect = w -> JoinWrapperUtil.addSelect(w, cols, null, dtoInfo);
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
     * 自定义where，会覆盖内置where组装
     *
     * @param conditions
     * @return
     */
    public MPJLambdaWrapperBuilder<T> where(List<Cond> conditions) {
        this.customWhere = w -> JoinWrapperUtil.addConditions(w, conditions, dtoInfo);
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

    /**
     * 自定义order，会覆盖内置order组装
     *
     * @param orders
     * @return
     */
    public MPJLambdaWrapperBuilder<T> order(List<Order> orders) {
        this.customOrder = w -> JoinWrapperUtil.addOrders(w, orders, dtoInfo);
        return this;
    }

    public MPJLambdaWrapper<T> build() {
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapper<>(mainClazz);
        customSelect.accept(wrapper);
        customJoin.accept(wrapper);
        if (customWhere != null) {
            customWhere.accept(wrapper);
        }
        if (customOrder != null) {
            customOrder.accept(wrapper);
        }
        return wrapper;
    }
}
