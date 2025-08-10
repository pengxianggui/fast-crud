package io.github.pengxianggui.crud.join;

import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.github.pengxianggui.crud.query.Query;

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
    private Consumer<MPJLambdaWrapper<T>> selectConsumer;
    private Consumer<MPJLambdaWrapper<T>> joinConsumer;
    private Consumer<MPJLambdaWrapper<T>> whereConsumer;
    private Consumer<MPJLambdaWrapper<T>> orderConsumer;
    private Consumer<MPJLambdaWrapper<T>> distinctConsumer;

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
        this.selectConsumer = w -> JoinWrapperUtil.addSelect(w, null, null, dtoInfo); // 默认查dto中所有(符合条件的)字段
        this.joinConsumer = w -> JoinWrapperUtil.addJoin(w, dtoInfo); // 默认从dto解析join信息
    }

    /**
     * 设置select
     *
     * @param customSelect 自定义的select信息
     * @return
     */
    public MPJLambdaWrapperBuilder<T> select(Consumer<MPJLambdaWrapper<T>> customSelect) {
        this.selectConsumer = customSelect;
        return this;
    }

    /**
     * 追加select
     *
     * @param customSelect 自定义的select信息
     * @return
     */
    public MPJLambdaWrapperBuilder<T> appendSelect(Consumer<MPJLambdaWrapper<T>> customSelect) {
        Consumer<MPJLambdaWrapper<T>> oldSelectConsumer = this.selectConsumer;
        this.selectConsumer = w -> {
            oldSelectConsumer.accept(w);
            customSelect.accept(w);
        };
        return this;
    }

    /**
     * 设置join
     *
     * @param customJoin 自定义的join信息
     * @return
     */
    public MPJLambdaWrapperBuilder<T> join(Consumer<MPJLambdaWrapper<T>> customJoin) {
        this.joinConsumer = customJoin;
        return this;
    }

    /**
     * 追加join
     *
     * @param customJoin 自定义的join信息
     * @return
     */
    public MPJLambdaWrapperBuilder<T> appendJoin(Consumer<MPJLambdaWrapper<T>> customJoin) {
        Consumer<MPJLambdaWrapper<T>> oldJoinConsumer = this.joinConsumer;
        this.joinConsumer = w -> {
            oldJoinConsumer.accept(w);
            customJoin.accept(w);
        };
        return this;
    }

    /**
     * 设置where条件
     *
     * @param customWhere 自定义的where信息
     * @return
     */
    public MPJLambdaWrapperBuilder<T> where(Consumer<MPJLambdaWrapper<T>> customWhere) {
        this.whereConsumer = customWhere;
        return this;
    }

    /**
     * 追加where条件
     *
     * @param customWhere 自定义的where信息
     * @return
     */
    public MPJLambdaWrapperBuilder<T> appendWhere(Consumer<MPJLambdaWrapper<T>> customWhere) {
        Consumer<MPJLambdaWrapper<T>> oldWhereConsumer = this.whereConsumer;
        this.whereConsumer = w -> {
            oldWhereConsumer.accept(w);
            customWhere.accept(w);
        };
        return this;
    }

    /**
     * 设置order
     *
     * @param customOrder 自定义的order信息
     * @return
     */
    public MPJLambdaWrapperBuilder<T> order(Consumer<MPJLambdaWrapper<T>> customOrder) {
        this.orderConsumer = customOrder;
        return this;
    }

    /**
     * 追加order
     *
     * @param customOrder 自定义的order信息
     * @return
     */
    public MPJLambdaWrapperBuilder<T> appendOrder(Consumer<MPJLambdaWrapper<T>> customOrder) {
        Consumer<MPJLambdaWrapper<T>> oldOrderConsumer = this.orderConsumer;
        this.orderConsumer = w -> {
            oldOrderConsumer.accept(w);
            customOrder.accept(w);
        };
        return this;
    }

    /**
     * 设置distinct
     *
     * @param distinct 是否针对select的字段进行distinct去重
     * @return
     */
    public MPJLambdaWrapperBuilder<T> distinct(boolean distinct) {
        this.distinctConsumer = w -> {
            if (distinct) {
                w.distinct();
            }
        };
        return this;
    }

    /**
     * 批量设置查询参数: 同时自定义select、where、order、distinct，会覆盖之前的设置!
     *
     * @param query
     * @return
     */
    public MPJLambdaWrapperBuilder<T> query(Query query) {
        this.selectConsumer = w -> JoinWrapperUtil.addSelect(w, query.getCols(), null, dtoInfo);
        this.whereConsumer = w -> JoinWrapperUtil.addConditions(w, query.getConds(), dtoInfo);
        this.orderConsumer = w -> JoinWrapperUtil.addOrders(w, query.getOrders(), dtoInfo);
        this.distinct(query.isDistinct());
        return this;
    }

    public MPJLambdaWrapper<T> build() {
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapper<>(mainClazz);
        selectConsumer.accept(wrapper);
        joinConsumer.accept(wrapper);
        if (whereConsumer != null) {
            whereConsumer.accept(wrapper);
        }
        if (orderConsumer != null) {
            orderConsumer.accept(wrapper);
        }
        if (distinctConsumer != null) {
            distinctConsumer.accept(wrapper);
        }
        return wrapper;
    }
}
