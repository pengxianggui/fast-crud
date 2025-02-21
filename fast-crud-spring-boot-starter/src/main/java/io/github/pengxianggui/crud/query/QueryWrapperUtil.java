package io.github.pengxianggui.crud.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.pengxianggui.crud.util.ColumnUtil;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class QueryWrapperUtil {

    public static <T> QueryWrapper addConditions(QueryWrapper<T> queryWrapper, List<Cond> conds, Class<?> entityClazz) {
        if (conds != null && conds.size() > 0) {
            Consumer<QueryWrapper<T>> wrapper = wrapper(queryWrapper, conds, entityClazz);
            wrapper.accept(queryWrapper);
        }
        return queryWrapper;
    }

    public static <T> QueryWrapper addSelect(QueryWrapper<T> queryWrapper, List<String> cols, boolean distinct, Class<?> entityClazz) {
        //查询字段
        if (cols != null && !cols.isEmpty()) {
            String[] selects = new String[cols.size()];
            for (int i = 0; i < cols.size(); i++) {
                String col = cols.get(i);
                if (distinct && i == 0) {
                    selects[i] = "distinct " + ColumnUtil.toDbField(col, entityClazz);
                } else {
                    selects[i] = ColumnUtil.toDbField(col, entityClazz);
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
                        if (cond.getRel() == Rel.AND) {
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

    public static <T> QueryWrapper addOrders(QueryWrapper<T> queryWrapper, List<Order> orders, Class<?> entityClazz) {
        if (orders != null) {
            for (Order order : orders) {
                if (order.isAsc()) {
                    queryWrapper.orderByAsc(ColumnUtil.toDbField(order.getCol(), entityClazz));
                } else {
                    queryWrapper.orderByDesc(ColumnUtil.toDbField(order.getCol(), entityClazz));
                }
            }
        }
        return queryWrapper;
    }

    private static <T> QueryWrapper addCondition(QueryWrapper<T> queryWrapper, Cond cond, Class<?> entityClazz) {
        String dbField = ColumnUtil.toDbField(cond.getCol(), entityClazz);
        switch (cond.getOpt()) {
            case EQ:
                if (cond.getRel() == Rel.AND) {
                    queryWrapper.eq(dbField, cond.getVal());
                } else {
                    queryWrapper.or(q -> q.eq(dbField, cond.getVal()));
                }
                break;
            case NE:
                if (cond.getRel() == Rel.AND) {
                    queryWrapper.ne(dbField, cond.getVal());
                } else {
                    queryWrapper.or(q -> q.ne(dbField, cond.getVal()));
                }
                break;
            case GT:
                if (cond.getRel() == Rel.AND) {
                    queryWrapper.gt(dbField, cond.getVal());
                } else {
                    queryWrapper.or(q -> q.gt(dbField, cond.getVal()));
                }
                break;
            case GE:
                if (cond.getRel() == Rel.AND) {
                    queryWrapper.ge(dbField, cond.getVal());
                } else {
                    queryWrapper.or(q -> q.ge(dbField, cond.getVal()));
                }
                break;
            case LT:
                if (cond.getRel() == Rel.AND) {
                    queryWrapper.lt(dbField, cond.getVal());
                } else {
                    queryWrapper.or(q -> q.lt(dbField, cond.getVal()));
                }
                break;
            case LE:
                if (cond.getRel() == Rel.AND) {
                    queryWrapper.le(dbField, cond.getVal());
                } else {
                    queryWrapper.or(q -> q.le(dbField, cond.getVal()));
                }
                break;
            case IN:
                if (cond.getRel() == Rel.AND) {
                    queryWrapper.in(dbField, (Collection) cond.getVal());
                } else {
                    queryWrapper.or(q -> q.in(dbField, cond.getVal()));
                }
                break;
            case NIN:
                if (cond.getRel() == Rel.AND) {
                    queryWrapper.notIn(dbField, (Collection) cond.getVal());
                } else {
                    queryWrapper.or(q -> q.notIn(dbField, cond.getVal()));
                }
                break;

            case LIKE:
                if (cond.getRel() == Rel.AND) {
                    queryWrapper.like(dbField, cond.getVal());
                } else {
                    queryWrapper.or(q -> q.like(dbField, cond.getVal()));
                }
                break;
            case NLIKE:
                if (cond.getRel() == Rel.AND) {
                    queryWrapper.notLike(dbField, cond.getVal());
                } else {
                    queryWrapper.or(q -> q.notLike(dbField, cond.getVal()));
                }
                break;
            case NULL:
                if (cond.getRel() == Rel.AND) {
                    queryWrapper.isNull(dbField);
                } else {
                    queryWrapper.or(q -> q.isNull(dbField));
                }
                break;
            case NNULL:
                if (cond.getRel() == Rel.AND) {
                    queryWrapper.isNotNull(dbField);
                } else {
                    queryWrapper.or(q -> q.isNotNull(dbField));
                }
                break;
            case EMPTY:
                if (cond.getRel() == Rel.AND) {
                    queryWrapper.nested(q -> q.isNull(dbField).or().eq(dbField, ""));
                } else {
                    queryWrapper.or(q -> q.nested(n -> n.isNull(dbField).or().eq(dbField, "")));
                }
                break;
            case NEMPTY:
                if (cond.getRel() == Rel.AND) {
                    queryWrapper.nested(q -> q.isNotNull(dbField).ne(dbField, ""));
                } else {
                    queryWrapper.or(q -> q.nested(n -> n.isNotNull(dbField).ne(dbField, "")));
                }
                break;
        }
        return queryWrapper;
    }

    public static <T> QueryWrapper<T> buildQueryWrapper(Query query, Class<T> clazz) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        //条件
        QueryWrapperUtil.addConditions(wrapper, query.getConds(), clazz);
        //排序
        QueryWrapperUtil.addOrders(wrapper, query.getOrders(), clazz);
        //查询字段
        QueryWrapperUtil.addSelect(wrapper, query.getCols(), query.isDistinct(), clazz);
        return wrapper;
    }
}
