package io.github.pengxianggui.crud.query.join;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.github.pengxianggui.crud.query.Cond;
import io.github.pengxianggui.crud.query.Order;
import io.github.pengxianggui.crud.query.Query;
import io.github.pengxianggui.crud.query.Rel;

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

    public static <T, DTO> DtoInfo getDtoInfo(Class<DTO> dtoClazz, Class<T> clazz) {
        if (CACHE.containsKey(dtoClazz)) {
            return CACHE.get(dtoClazz);
        }
        DtoInfo<DTO, ?> dtoInfo = new DtoInfo<>(dtoClazz, clazz);
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
        DtoInfo<DTO, T> dtoInfo = getDtoInfo(dtoClazz, clazz);
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

    private static <DTO, T> void addSelect(MPJLambdaWrapper<T> wrapper, List<String> cols, boolean distinct, DtoInfo<DTO, T> dtoInfo) {
        List<DtoInfo.FieldRelate> fieldRelates = dtoInfo.getFields();
        fieldRelates.stream().filter(field -> cols == null || cols.size() == 0 || cols.contains(field.getField().getName()))
                .forEach(field -> wrapper.selectAs(field.getFieldGetter(), field.getTargetFieldGetter()));
        if (distinct) {
            wrapper.distinct();
        }
    }

    private static <DTO, T> void addJoin(MPJLambdaWrapper<T> wrapper, DtoInfo<DTO, T> dtoInfo) {
        Map<JoinType, List<DtoInfo.JoinInfo<T>>> joinMap = dtoInfo.getJoinMap();
        if (joinMap == null) {
            return;
        }
        joinMap.forEach((joinType, joinInfos) -> {
            joinInfos.forEach(joinInfo -> {
                Class<?> joinToClass = joinInfo.getJoin().value();
                switch (joinType) {
                    case INNER:
                        wrapper.innerJoin(joinToClass, on -> joinInfo.apply(on));
                        break;
                    case LEFT:
                        wrapper.leftJoin(joinToClass, on -> joinInfo.apply(on));
                        break;
                    case RIGHT:
                        wrapper.rightJoin(joinToClass, on -> joinInfo.apply(on));
                        break;
                }
            });
        });
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
        DtoInfo.FieldRelate fieldRelate = dtoInfo.getField(cond.getCol());
        if (fieldRelate == null) {
            throw new IllegalArgumentException(cond.getCol() + "必须是dto的属性:" + dtoInfo.getDtoClass());
        }
        boolean effect = true;
        if (cond.getVal() == null || (cond.getVal() instanceof CharSequence && StrUtil.isBlank((CharSequence) cond.getVal()))) {
            effect = false;
        }
        SFunction<T, ?> targetFieldGetter = fieldRelate.getTargetFieldGetter();
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

    private static <DTO, T> void addOrders(MPJLambdaWrapper<T> wrapper, List<Order> orders, DtoInfo<DTO, T> dtoInfo) {
        if (orders == null) {
            return;
        }
        for (Order order : orders) {
            DtoInfo.FieldRelate fieldRelate = dtoInfo.getField(order.getCol());
            if (fieldRelate == null) {
                throw new IllegalArgumentException(order.getCol() + "必须是dto的属性:" + dtoInfo.getDtoClass());
            }
            if (order.isAsc()) {
                wrapper.orderByAsc(fieldRelate.getTargetFieldGetter());
            } else {
                wrapper.orderByDesc(fieldRelate.getTargetFieldGetter());
            }
        }
    }
}
