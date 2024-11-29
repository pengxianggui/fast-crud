package io.github.pengxianggui.crud.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class QueryWrapperUtil {

    public static <T> QueryWrapper addConditions(QueryWrapper<T> queryWrapper, List<Cond> conds) {
        //条件
        if (conds != null && conds.size() > 0) {
            Consumer<QueryWrapper<T>> wrapper = wrapper(queryWrapper, conds);
            wrapper.accept(queryWrapper);
        }
        return queryWrapper;
    }

    public static <T> QueryWrapper addSelect(QueryWrapper<T> queryWrapper, String[] cols) {
        return addSelect(queryWrapper, cols, false);
    }

    public static <T> QueryWrapper addSelect(QueryWrapper<T> queryWrapper, String[] cols, boolean distinct) {
        //查询字段
        if (cols != null && cols.length > 0) {
            String[] selects = new String[cols.length];
            for (int i = 0; i < cols.length; i++) {
                if (distinct && i == 0) {
                    selects[i] = "distinct " + ColumnMapperUtil.map(cols[i]);
                } else {
                    selects[i] = ColumnMapperUtil.map(cols[i]);
                }
            }
            queryWrapper.select(selects);
        }
        return queryWrapper;
    }

    private static <T> Consumer<QueryWrapper<T>> wrapper(QueryWrapper<T> queryWrapper, List<Cond> conds) {
        return qw -> {
            for (int i = 0; i < conds.size(); i++) {
                Cond cond = conds.get(i);
                if (cond.getConds() != null && cond.getConds().size() > 0) {
                    if (i == 0) {
                        qw.nested(wrapper(queryWrapper, cond.getConds()));
                    } else {
                        if (cond.getRel() == Cond.Relation.AND) {
                            qw.and(wrapper(queryWrapper, cond.getConds()));
                        } else {
                            qw.or(wrapper(queryWrapper, cond.getConds()));
                        }
                    }
                } else {
                    addCondition(qw, cond);
                }
            }
        };
    }

    public static <T> QueryWrapper addQueryOrders(QueryWrapper<T> queryWrapper, List<Order> orders) {
        if (orders != null) {
            orders.forEach((order) -> {
                if (order != null) {
                    if (order.isAsc()) {
                        queryWrapper.orderByAsc(ColumnMapperUtil.map(order.getCol()));
                    } else {
                        queryWrapper.orderByDesc(ColumnMapperUtil.map(order.getCol()));
                    }
                }
            });
        }
        return queryWrapper;
    }

    public static <T> QueryWrapper addOrders(QueryWrapper<T> queryWrapper, List<OrderItem> orders) {
        if (orders != null) {
            for (OrderItem order : orders) {
                if (order.isAsc()) {
                    queryWrapper.orderByAsc(order.getColumn());
                } else {
                    queryWrapper.orderByDesc(order.getColumn());
                }
            }
        }
        return queryWrapper;
    }

    public static <T> QueryWrapper addCondition(QueryWrapper<T> queryWrapper, Cond cond) {
        switch (cond.getOpt()) {
            case EQ:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.eq(ColumnMapperUtil.map(cond.getCol()), cond.getVal());
                } else {
                    queryWrapper.or(q -> q.eq(ColumnMapperUtil.map(cond.getCol()), cond.getVal()));
                }
                break;
            case NE:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.ne(ColumnMapperUtil.map(cond.getCol()), cond.getVal());
                } else {
                    queryWrapper.or(q -> q.ne(ColumnMapperUtil.map(cond.getCol()), cond.getVal()));
                }
                break;
            case GT:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.gt(ColumnMapperUtil.map(cond.getCol()), cond.getVal());
                } else {
                    queryWrapper.or(q -> q.gt(ColumnMapperUtil.map(cond.getCol()), cond.getVal()));
                }
                break;
            case GE:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.ge(ColumnMapperUtil.map(cond.getCol()), cond.getVal());
                } else {
                    queryWrapper.or(q -> q.ge(ColumnMapperUtil.map(cond.getCol()), cond.getVal()));
                }
                break;
            case LT:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.lt(ColumnMapperUtil.map(cond.getCol()), cond.getVal());
                } else {
                    queryWrapper.or(q -> q.lt(ColumnMapperUtil.map(cond.getCol()), cond.getVal()));
                }
                break;
            case LE:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.le(ColumnMapperUtil.map(cond.getCol()), cond.getVal());
                } else {
                    queryWrapper.or(q -> q.le(ColumnMapperUtil.map(cond.getCol()), cond.getVal()));
                }
                break;
            case IN:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.in(ColumnMapperUtil.map(cond.getCol()), (Collection) cond.getVal());
                } else {
                    queryWrapper.or(q -> q.in(ColumnMapperUtil.map(cond.getCol()), cond.getVal()));
                }
                break;
            case NIN:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.notIn(ColumnMapperUtil.map(cond.getCol()), (Collection) cond.getVal());
                } else {
                    queryWrapper.or(q -> q.notIn(ColumnMapperUtil.map(cond.getCol()), cond.getVal()));
                }
                break;

            case LIKE:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.like(ColumnMapperUtil.map(cond.getCol()), cond.getVal());
                } else {
                    queryWrapper.or(q -> q.like(ColumnMapperUtil.map(cond.getCol()), cond.getVal()));
                }
                break;
            case NLIKE:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.notLike(ColumnMapperUtil.map(cond.getCol()), cond.getVal());
                } else {
                    queryWrapper.or(q -> q.notLike(ColumnMapperUtil.map(cond.getCol()), cond.getVal()));
                }
                break;
            case NULL:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.isNull(ColumnMapperUtil.map(cond.getCol()));
                } else {
                    queryWrapper.or(q -> q.isNull(ColumnMapperUtil.map(cond.getCol())));
                }
                break;
            case NNULL:
                if (cond.getRel() == Cond.Relation.AND) {
                    queryWrapper.isNotNull(ColumnMapperUtil.map(cond.getCol()));
                } else {
                    queryWrapper.or(q -> q.isNotNull(ColumnMapperUtil.map(cond.getCol())));
                }
                break;
        }
        return queryWrapper;
    }
}
