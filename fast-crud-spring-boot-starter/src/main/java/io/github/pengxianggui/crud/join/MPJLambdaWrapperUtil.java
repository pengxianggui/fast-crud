package io.github.pengxianggui.crud.join;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.github.pengxianggui.crud.query.Cond;
import io.github.pengxianggui.crud.query.Order;
import io.github.pengxianggui.crud.query.Query;
import io.github.pengxianggui.crud.query.Rel;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author pengxg
 * @date 2025/5/23 16:04
 */
public class MPJLambdaWrapperUtil {
    private static final Map<Class<?>, DtoInfo> CACHE = new ConcurrentHashMap<>();

    public static <DTO> DtoInfo getDtoInfo(Class<DTO> dtoClazz) {
        if (CACHE.containsKey(dtoClazz)) {
            return CACHE.get(dtoClazz);
        }
        DtoInfo dtoInfo = new DtoInfo(dtoClazz);
        CACHE.put(dtoClazz, dtoInfo);
        return dtoInfo;
    }

    /**
     * 构造MPJLambdaWrapper实例，通过解析dtoClazz类和clazz类，将query解析为查询条件，构造MPJLambdaWrapper实例
     *
     * @param query    查询条件
     * @param clazz    主表对应的实体类
     * @param dtoClazz dto类，结果类
     * @param <T>      主表对应的实体类泛型
     * @param <DTO>    dto类泛型
     * @return 返回MPJLambdaWrapper实例
     */
    public static <T, DTO> MPJLambdaWrapper<T> build(Query query, Class<T> clazz, Class<DTO> dtoClazz) {
        DtoInfo dtoInfo = getDtoInfo(dtoClazz);
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

    // TODO 上述build方法是全包，扩展性不好，提供一个可定制的半包build

    private static <T> void addSelect(MPJLambdaWrapper<T> wrapper, List<String> cols, boolean distinct, DtoInfo dtoInfo) {
        List<DtoInfo.DtoField> dtoFields = dtoInfo.getFields();
        dtoFields.stream().filter(field -> cols == null || cols.size() == 0 || cols.contains(field.getField().getName()))
                .forEach(field -> {
                    if (field.isJoinIgnore()) {
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
                                        if (field1.isJoinIgnore() || field1.targetFieldNotExist()) {
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
                                    if (field1.isJoinIgnore()) {
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

    private static <T> void addJoin(MPJLambdaWrapper<T> wrapper, DtoInfo dtoInfo) {
        List<DtoInfo.JoinInfo> innerJoins = dtoInfo.getInnerJoinInfo();
        if (innerJoins != null && !innerJoins.isEmpty()) {
            innerJoins.forEach(join -> wrapper.innerJoin(join.getTargetEntityClass(), on -> join.apply(on)));
        }
        List<DtoInfo.JoinInfo> leftJoins = dtoInfo.getLeftJoinInfo();
        if (leftJoins != null && !leftJoins.isEmpty()) {
            leftJoins.forEach(join -> wrapper.leftJoin(join.getTargetEntityClass(), on -> join.apply(on)));
        }
        List<DtoInfo.JoinInfo> rightJoins = dtoInfo.getRightJoinInfo();
        if (rightJoins != null && !rightJoins.isEmpty()) {
            rightJoins.forEach(join -> wrapper.rightJoin(join.getTargetEntityClass(), on -> join.apply(on)));
        }
    }

    private static <T> MPJLambdaWrapper<T> addConditions(MPJLambdaWrapper<T> wrapper, List<Cond> conds, DtoInfo dtoInfo) {
        if (conds == null || conds.isEmpty()) {
            return wrapper;
        }
        Consumer<MPJLambdaWrapper<T>> consumer = wrapperConsumer(conds, Rel.AND, dtoInfo);
        consumer.accept(wrapper);
        return wrapper;
    }

    private static <T> Consumer<MPJLambdaWrapper<T>> wrapperConsumer(List<Cond> conds, Rel rel, DtoInfo dtoInfo) {
        return w -> {
            for (int i = 0; i < conds.size(); i++) {
                Cond cond = conds.get(i);
                if (cond.getConds() != null && cond.getConds().size() > 0) {
                    w.nested(wrapperConsumer(cond.getConds(), rel, dtoInfo));
                } else {
                    addCondition(w, cond, rel, dtoInfo);
                }
            }
        };
    }

    private static <T> void addCondition(MPJLambdaWrapper<T> wrapper, Cond cond, Rel rel, DtoInfo dtoInfo) {
        DtoInfo.DtoField dtoField = dtoInfo.getField(cond.getCol());
        if (dtoField.isJoinIgnore()) {
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

    private static <T> void addOrders(MPJLambdaWrapper<T> wrapper, List<Order> orders, DtoInfo dtoInfo) {
        if (orders == null) {
            return;
        }
        for (Order order : orders) {
            DtoInfo.DtoField dtoField = dtoInfo.getField(order.getCol());
            if (dtoField.isJoinIgnore()) {
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
}
