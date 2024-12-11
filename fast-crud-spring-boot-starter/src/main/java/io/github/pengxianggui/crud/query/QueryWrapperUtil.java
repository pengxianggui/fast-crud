package io.github.pengxianggui.crud.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class QueryWrapperUtil {

    public static <T> QueryWrapper addConditions(QueryWrapper<T> queryWrapper, List<Cond> conds, Class<?> entityClazz) {
        //条件
        if (conds != null && conds.size() > 0) {
            Consumer<QueryWrapper<T>> wrapper = wrapper(queryWrapper, conds, entityClazz);
            wrapper.accept(queryWrapper);
        }
        return queryWrapper;
    }

    public static <T> QueryWrapper addSelect(QueryWrapper<T> queryWrapper, String[] cols, Class<T> entityClazz) {
        return addSelect(queryWrapper, cols, false, entityClazz);
    }

    public static <T> QueryWrapper addSelect(QueryWrapper<T> queryWrapper, String[] cols, boolean distinct, Class<?> entityClazz) {
        //查询字段
        if (cols != null && cols.length > 0) {
            String[] selects = new String[cols.length];
            for (int i = 0; i < cols.length; i++) {
                if (distinct && i == 0) {
                    selects[i] = "distinct " + ColumnMapperUtil.toDbField(cols[i], entityClazz);
                } else {
                    selects[i] = ColumnMapperUtil.toDbField(cols[i], entityClazz);
                }
            }
            queryWrapper.select(selects);
        }
        return queryWrapper;
    }

    private static <T> Consumer<QueryWrapper<T>> wrapper(QueryWrapper<T> queryWrapper, List<Cond> conds, Class<?> entityClazz) {
        return qw -> {
            for (int i = 0; i < conds.size(); i++) {
                Cond cond = conds.get(i);
                if (cond.getConds() != null && cond.getConds().size() > 0) {
                    if (i == 0) {
                        qw.nested(wrapper(queryWrapper, cond.getConds(), entityClazz));
                    } else {
                        if (cond.getRel() == Cond.Relation.AND) {
                            qw.and(wrapper(queryWrapper, cond.getConds(), entityClazz));
                        } else {
                            qw.or(wrapper(queryWrapper, cond.getConds(), entityClazz));
                        }
                    }
                } else {
                    addCondition(qw, cond, entityClazz);
                }
            }
        };
    }

    public static <T> QueryWrapper addQueryOrders(QueryWrapper<T> queryWrapper, List<Order> orders, Class<T> entityClazz) {
        if (orders != null) {
            orders.forEach((order) -> {
                if (order != null) {
                    if (order.isAsc()) {
                        queryWrapper.orderByAsc(ColumnMapperUtil.toDbField(order.getCol(), entityClazz));
                    } else {
                        queryWrapper.orderByDesc(ColumnMapperUtil.toDbField(order.getCol(), entityClazz));
                    }
                }
            });
        }
        return queryWrapper;
    }

    public static <T> QueryWrapper addOrders(QueryWrapper<T> queryWrapper, List<OrderItem> orders, Class<?> entityClazz) {
        if (orders != null) {
            for (OrderItem order : orders) {
                if (order.isAsc()) {
                    queryWrapper.orderByAsc(ColumnMapperUtil.toDbField(order.getColumn(), entityClazz));
                } else {
                    queryWrapper.orderByDesc(ColumnMapperUtil.toDbField(order.getColumn(), entityClazz));
                }
            }
        }
        return queryWrapper;
    }

    private static <T> QueryWrapper addCondition(QueryWrapper<T> queryWrapper, Cond cond, Class<?> entityClazz) {
        switch (cond.getOpt()) {
            case EQ:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.eq(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal());
                } else {
                    queryWrapper.or(q -> q.eq(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal()));
                }
                break;
            case NE:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.ne(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal());
                } else {
                    queryWrapper.or(q -> q.ne(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal()));
                }
                break;
            case GT:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.gt(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal());
                } else {
                    queryWrapper.or(q -> q.gt(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal()));
                }
                break;
            case GE:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.ge(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal());
                } else {
                    queryWrapper.or(q -> q.ge(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal()));
                }
                break;
            case LT:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.lt(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal());
                } else {
                    queryWrapper.or(q -> q.lt(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal()));
                }
                break;
            case LE:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.le(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal());
                } else {
                    queryWrapper.or(q -> q.le(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal()));
                }
                break;
            case IN:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.in(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), (Collection) cond.getVal());
                } else {
                    queryWrapper.or(q -> q.in(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal()));
                }
                break;
            case NIN:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.notIn(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), (Collection) cond.getVal());
                } else {
                    queryWrapper.or(q -> q.notIn(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal()));
                }
                break;

            case LIKE:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.like(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal());
                } else {
                    queryWrapper.or(q -> q.like(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal()));
                }
                break;
            case NLIKE:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.notLike(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal());
                } else {
                    queryWrapper.or(q -> q.notLike(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz), cond.getVal()));
                }
                break;
            case NULL:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.isNull(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz));
                } else {
                    queryWrapper.or(q -> q.isNull(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz)));
                }
                break;
            case NNULL:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.isNotNull(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz));
                } else {
                    queryWrapper.or(q -> q.isNotNull(ColumnMapperUtil.toDbField(cond.getCol(), entityClazz)));
                }
                break;
        }
        return queryWrapper;
    }
}
