package io.github.pengxianggui.crud.join;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.yulichang.wrapper.*;
import com.github.yulichang.wrapper.interfaces.QueryJoin;
import io.github.pengxianggui.crud.query.Cond;
import io.github.pengxianggui.crud.query.Order;
import io.github.pengxianggui.crud.query.Query;
import io.github.pengxianggui.crud.query.Rel;
import io.github.pengxianggui.crud.util.EntityUtil;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author pengxg
 * @date 2025/6/15 19:52
 */
public class JoinWrapperUtil {
    private static final Map<Class<?>, DtoInfo> CACHE = new ConcurrentHashMap<>();

    static DtoInfo getDtoInfo(Class<?> dtoClazz) {
        DtoInfo dtoInfo;
        if (CACHE.containsKey(dtoClazz)) {
            dtoInfo = CACHE.get(dtoClazz);
        } else {
            dtoInfo = new DtoInfo(dtoClazz);
            CACHE.put(dtoClazz, dtoInfo);
        }
        if (dtoInfo == null) {
            throw new ClassJoinParseException(dtoClazz, "Can not found dtoInfo of dtoClass:" + dtoClazz.getName());
        }
        return dtoInfo;
    }


    /**
     * 构造MPJLambdaWrapper实例，通过解析dtoClazz类和clazz类，将query解析为查询条件，构造MPJLambdaWrapper实例
     *
     * @param query    查询条件
     * @param clazz    主表对应的实体类
     * @param dtoClazz dto类，结果类
     * @param <T>      主表对应的实体类泛型
     * @return 返回MPJLambdaWrapper实例
     */
    public static <T> MPJLambdaWrapper<T> buildMPJLambdaWrapper(Query query, Class<T> clazz, Class<?> dtoClazz) {
        DtoInfo dtoInfo = JoinWrapperUtil.getDtoInfo(dtoClazz);
        if (dtoInfo == null) {
            throw new ClassJoinParseException(dtoClazz, "Can not found dtoInfo of dtoClass:" + dtoClazz.getName());
        }
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapper<>(clazz);
        // 查询字段
        addSelect(wrapper, query.getCols(), query.isDistinct(), dtoInfo);
        // join
        addJoin(wrapper, dtoInfo);
        // 条件
        addConditions(wrapper, query.getConds(), dtoInfo);
        // 排序
        addOrders(wrapper, query.getOrders(), dtoInfo);
        return wrapper;
    }

    static <T> void addSelect(MPJLambdaWrapper<T> wrapper, List<String> cols, boolean distinct, DtoInfo dtoInfo) {
        List<DtoInfo.DtoField> dtoFields = dtoInfo.getFields();
        dtoFields.stream().filter(field -> cols == null || cols.size() == 0 || cols.contains(field.getField().getName()))
                .forEach(field -> {
                    if (field.isJoinIgnoreForQuery()) {
                        return;
                    }

                    if (field.isDbField()) { // 无论是什么类型(简单类型、集合、对象类型),标记为数据库字段则直接select
                        wrapper.selectAs(field.getTargetFieldGetter(), field.getFieldGetter());
                        return;
                    }

                    if (Collection.class.isAssignableFrom(field.getField().getType())) { // 一对多
                        Type type = TypeUtil.getTypeArgument(field.getField().getGenericType());
                        if (ClassUtil.isSimpleValueType(TypeUtil.getClass(type))) { // 单字段
                            wrapper.selectCollection(field.getTargetClazz(), field.getFieldGetter(), map -> {
                                map.result(field.getTargetFieldGetter());
                                return map;
                            });
                        } else {
                            if (field.getTargetClazz() == type) {
                                wrapper.selectCollection(field.getTargetClazz(), field.getFieldGetter());
                            } else {
                                wrapper.selectCollection(field.getTargetClazz(), field.getFieldGetter(), map -> {
                                    DtoInfo subDtoInfo = new DtoInfo(TypeUtil.getClass(type));
                                    // 这里面暂不考虑递归情况, 否则太复杂, 还要牵扯子查询, mybatis-plus-join怕无法支撑
                                    subDtoInfo.getFields().forEach(field1 -> {
                                        if (field1.isJoinIgnoreForQuery() || field1.targetFieldNotExist()) {
                                            return;
                                        }
                                        map.result(field1.getTargetFieldGetter(), field1.getFieldGetter());
                                    });
                                    return map;
                                });
                            }
                        }
                    } else { // 一对一
                        if (ClassUtil.isSimpleValueType(field.getField().getType())) { // 单字段
                            if (field.targetFieldNotExist()) {
                                return;
                            }
                            wrapper.selectAs(field.getTargetFieldGetter(), field.getFieldGetter());
                        } else { // 一对一映射实体
                            Type type = field.getField().getType();
                            wrapper.selectAssociation(field.getTargetClazz(), field.getFieldGetter(), map -> {
                                DtoInfo subDtoInfo = new DtoInfo(TypeUtil.getClass(type), field.getTargetClazz());
                                // 这里面暂不考虑递归情况, 否则太复杂, 还要牵扯子查询, mybatis-plus-join怕无法支撑
                                subDtoInfo.getFields().forEach(field1 -> {
                                    if (field1.isJoinIgnoreForQuery()) {
                                        return;
                                    }
                                    map.result(field1.getTargetFieldGetter(), field1.getFieldGetter());
                                });
                                return map;
                            });
                        }
                    }
                });
        if (distinct) {
            wrapper.distinct();
        }
    }

    static <T> void addJoin(QueryJoin<? extends JoinAbstractLambdaWrapper<T, ? extends JoinAbstractLambdaWrapper>, T> wrapper,
                            DtoInfo dtoInfo) {
        List<DtoInfo.JoinInfo> innerJoins = dtoInfo.getInnerJoinInfo();
        if (innerJoins != null && !innerJoins.isEmpty()) {
            innerJoins.forEach(join -> wrapper.innerJoin(join.getJoinEntityClass(), on -> {
                addJoinConditions(on, join.getCondFieldRelates());
                return on;
            }));
        }
        List<DtoInfo.JoinInfo> leftJoins = dtoInfo.getLeftJoinInfo();
        if (leftJoins != null && !leftJoins.isEmpty()) {
            leftJoins.forEach(join -> wrapper.leftJoin(join.getJoinEntityClass(), on -> {
                addJoinConditions(on, join.getCondFieldRelates());
                return on;
            }));
        }
        List<DtoInfo.JoinInfo> rightJoins = dtoInfo.getRightJoinInfo();
        if (rightJoins != null && !rightJoins.isEmpty()) {
            rightJoins.forEach(join -> wrapper.rightJoin(join.getJoinEntityClass(), on -> {
                addJoinConditions(on, join.getCondFieldRelates());
                return on;
            }));
        }
    }

    public static <T> void addJoin(DeleteJoinWrapper<T> wrapper, Class<?> dtoClazz) {
        DtoInfo dtoInfo = getDtoInfo(dtoClazz);
        addJoin(wrapper, dtoInfo);
    }

    static <T, C extends JoinAbstractWrapper<T, C>> void addJoinConditions(JoinAbstractWrapper<T, C> on,
                                                                           DtoInfo.OnCondition[] condFieldRelates) {
        for (DtoInfo.OnCondition condFieldRelate : condFieldRelates) {
            SFunction<?, ?> fieldGetter = condFieldRelate.getFieldGetter();
            SFunction<?, ?> targetFieldGetter = condFieldRelate.getTargetFieldGetter();
            switch (condFieldRelate.getOpt()) {
                case EQ:
                    on.eq(fieldGetter, targetFieldGetter);
                    break;
                case NE:
                    on.ne(fieldGetter, targetFieldGetter);
                    break;
                case GT:
                    on.gt(fieldGetter, targetFieldGetter);
                    break;
                case GE:
                    on.ge(fieldGetter, targetFieldGetter);
                    break;
                case LT:
                    on.lt(fieldGetter, targetFieldGetter);
                    break;
                case LE:
                    on.le(fieldGetter, targetFieldGetter);
                    break;
                case IN:
                    on.in(fieldGetter, targetFieldGetter);
                    break;
                case NIN:
                    on.notIn(fieldGetter, targetFieldGetter);
                    break;
                case LIKE:
                    on.like(fieldGetter, targetFieldGetter);
                    break;
                case NLIKE:
                    on.notLike(fieldGetter, targetFieldGetter);
                    break;
                case NULL:
                    on.isNull(fieldGetter);
                    break;
                case NNULL:
                    on.isNotNull(fieldGetter);
                    break;
                case EMPTY:
                    on.nested(q -> q.isNull(fieldGetter).or().eq(fieldGetter, ""));
                    break;
                case NEMPTY:
                    on.nested(q -> q.isNotNull(fieldGetter).ne(fieldGetter, ""));
                    break;
            }
        }
    }

    static <T, C extends JoinAbstractWrapper<T, C>> void addConditions(JoinAbstractWrapper<T, C> wrapper,
                                                                       List<Cond> conds,
                                                                       DtoInfo dtoInfo) {
        if (conds == null || conds.isEmpty()) {
            return;
        }
        Consumer<JoinAbstractWrapper<T, C>> consumer = wrapperConsumer(conds, Rel.AND, dtoInfo);
        consumer.accept(wrapper);
    }

    public static <T, C extends JoinAbstractWrapper<T, C>> void addConditions(JoinAbstractWrapper<T, C> wrapper,
                                                                              List<Cond> conds,
                                                                              Class<?> dtoClazz) {
        DtoInfo dtoInfo = getDtoInfo(dtoClazz);
        addConditions(wrapper, conds, dtoInfo);
    }

    static <T, C extends JoinAbstractWrapper<T, C>> Consumer<JoinAbstractWrapper<T, C>> wrapperConsumer(List<Cond> conds, Rel rel, DtoInfo dtoInfo) {
        return w -> {
            for (int i = 0; i < conds.size(); i++) {
                Cond cond = conds.get(i);
                if (cond.getConds() != null && cond.getConds().size() > 0) {
                    w.nested((Consumer<C>) wrapperConsumer(cond.getConds(), rel, dtoInfo));
                } else {
                    addCondition(w, cond, rel, dtoInfo);
                }
            }
        };
    }

    static <T> void addCondition(JoinAbstractWrapper<T, ?> wrapper, Cond cond, Rel rel, DtoInfo dtoInfo) {
        DtoInfo.DtoField dtoField = dtoInfo.getField(cond.getCol());
        if (dtoField.isJoinIgnoreForQuery()) {
            return;
        }

        if (dtoField == null) {
            throw new IllegalArgumentException(cond.getCol() + "必须是dto的属性:" + dtoInfo.getDtoClazz());
        }
        // 条件是否生效
        boolean effect = true;
        if (cond.getVal() == null || (cond.getVal() instanceof CharSequence && StrUtil.isBlank((CharSequence) cond.getVal()))) {
            effect = false;
        }
        SFunction<T, ?> targetFieldGetter = dtoField.getTargetFieldGetter();
        switch (cond.getOpt()) {
            case EQ:
                if (rel == Rel.AND) {
                    wrapper.eq(effect, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.eq(targetFieldGetter, cond.getVal()));
                }
                break;
            case NE:
                if (rel == Rel.AND) {
                    wrapper.ne(effect, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.ne(targetFieldGetter, cond.getVal()));
                }
                break;
            case GT:
                if (rel == Rel.AND) {
                    wrapper.gt(effect, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.gt(targetFieldGetter, cond.getVal()));
                }
                break;
            case GE:
                if (rel == Rel.AND) {
                    wrapper.ge(effect, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.ge(targetFieldGetter, cond.getVal()));
                }
                break;
            case LT:
                if (rel == Rel.AND) {
                    wrapper.lt(effect, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.lt(targetFieldGetter, cond.getVal()));
                }
                break;
            case LE:
                if (rel == Rel.AND) {
                    wrapper.le(effect, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.le(targetFieldGetter, cond.getVal()));
                }
                break;
            case IN:
                if (rel == Rel.AND) {
                    wrapper.in(effect, targetFieldGetter, (Collection) cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.in(targetFieldGetter, cond.getVal()));
                }
                break;
            case NIN:
                if (rel == Rel.AND) {
                    wrapper.notIn(effect, targetFieldGetter, (Collection) cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.notIn(targetFieldGetter, cond.getVal()));
                }
                break;
            case LIKE:
                if (rel == Rel.AND) {
                    wrapper.like(effect, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.like(targetFieldGetter, cond.getVal()));
                }
                break;
            case NLIKE:
                if (rel == Rel.AND) {
                    wrapper.notLike(effect, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.notLike(targetFieldGetter, cond.getVal()));
                }
                break;
            case NULL:
                if (rel == Rel.AND) {
                    wrapper.isNull(targetFieldGetter);
                } else {
                    wrapper.or(q -> q.isNull(targetFieldGetter));
                }
                break;
            case NNULL:
                if (rel == Rel.AND) {
                    wrapper.isNotNull(targetFieldGetter);
                } else {
                    wrapper.or(q -> q.isNotNull(targetFieldGetter));
                }
                break;
            case EMPTY:
                if (rel == Rel.AND) {
                    wrapper.nested(q -> q.isNull(targetFieldGetter).or().eq(targetFieldGetter, ""));
                } else {
                    wrapper.or(q -> q.nested(n -> n.isNull(targetFieldGetter).or().eq(targetFieldGetter, "")));
                }
                break;
            case NEMPTY:
                if (rel == Rel.AND) {
                    wrapper.nested(q -> q.isNotNull(targetFieldGetter).ne(targetFieldGetter, ""));
                } else {
                    wrapper.or(q -> q.nested(n -> n.isNotNull(targetFieldGetter).ne(targetFieldGetter, "")));
                }
                break;
        }
    }

    static <T> void addOrders(MPJLambdaWrapper<T> wrapper, List<Order> orders, DtoInfo dtoInfo) {
        if (orders == null) {
            return;
        }
        for (Order order : orders) {
            DtoInfo.DtoField dtoField = dtoInfo.getField(order.getCol());
            if (dtoField.isJoinIgnoreForQuery()) {
                continue;
            }

            if (dtoField == null) {
                throw new IllegalArgumentException(order.getCol() + "必须是dto的属性:" + dtoInfo.getDtoClazz());
            }
            if (order.isAsc()) {
                wrapper.orderByAsc(dtoField.getTargetFieldGetter());
            } else {
                wrapper.orderByDesc(dtoField.getTargetFieldGetter());
            }
        }
    }

    public static <T> void addOrders(MPJLambdaWrapper<T> wrapper, List<Order> orders, Class<?> dtoClazz) {
        DtoInfo dtoInfo = JoinWrapperUtil.getDtoInfo(dtoClazz);
        addOrders(wrapper, orders, dtoInfo);
    }

    static <T> void addSet(UpdateJoinWrapper<T> wrapper, DtoInfo dtoInfo, Object dto, boolean updateNull) {
        List<DtoInfo.DtoField> dtoFields = dtoInfo.getFields();
        for (DtoInfo.DtoField field : dtoFields) {
            if (field.isJoinIgnoreForUpdate() || field.targetFieldNotExist() || field.isPk()) {
                continue;
            }

            try {
                field.getField().setAccessible(true);
                Object fieldValue = field.getField().get(dto);

                if (!EntityUtil.fieldNeedUpdate(field.getTargetField(), fieldValue, updateNull)) {
                    continue;
                }
                if (field.isDbField()) { // 无论是什么类型(简单类型、集合、对象类型),标记为数据库字段则直接set
                    wrapper.set(field.getTargetFieldGetter(), fieldValue);
                    continue;
                }
                if (Collection.class.isAssignableFrom(field.getField().getType())) { // 一对多
                    Type type = TypeUtil.getTypeArgument(field.getField().getGenericType());
                    if (ClassUtil.isSimpleValueType(TypeUtil.getClass(type))) { // 简单字段
                        wrapper.set(field.getTargetFieldGetter(), fieldValue);
                    } else {
                        // 一对多映射实体不级联更新
                    }
                } else { // 一对一
                    if (ClassUtil.isSimpleValueType(field.getField().getType())) { // 单字段
                        wrapper.set(field.getTargetFieldGetter(), fieldValue);
                    } else { // 一对一映射实体暂
                        if (fieldValue == null) {
                            continue;
                        }
                        Type type = field.getField().getType();
                        DtoInfo subDtoInfo = new DtoInfo(TypeUtil.getClass(type), field.getTargetClazz());
                        // 这里面暂不考虑递归情况, 否则太复杂, 还要牵扯子查询, mybatis-plus-join怕无法支撑
                        for (DtoInfo.DtoField subDtoInfoField : subDtoInfo.getFields()) {
                            if (subDtoInfoField.isJoinIgnoreForUpdate() || subDtoInfoField.targetFieldNotExist()) {
                                continue;
                            }
                            wrapper.set(subDtoInfoField.getTargetFieldGetter(), subDtoInfoField.getField().get(fieldValue));
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
