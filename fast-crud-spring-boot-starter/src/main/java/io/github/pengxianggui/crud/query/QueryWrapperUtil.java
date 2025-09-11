package io.github.pengxianggui.crud.query;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import io.github.pengxianggui.crud.util.ColumnUtil;
import io.github.pengxianggui.crud.util.EntityUtil;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class QueryWrapperUtil {

    /**
     * 构造QueryWrapper实例，通过解析query，将query解析为查询条件，构造QueryWrapper实例
     *
     * @param query 查询条件
     * @param clazz 实体类
     * @param <T>   实体类泛型
     * @return 返回QueryWrapper实例
     */
    public static <T> QueryWrapper<T> build(Query query, Class<T> clazz) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        //查询字段
        addSelect(wrapper, query.getCols(), query.isDistinct(), clazz);
        //条件
        addConditions(wrapper, query.getConds(), clazz);
        //排序
        addOrders(wrapper, query.getOrders(), clazz);
        return wrapper;
    }

    private static <T> QueryWrapper addSelect(QueryWrapper<T> queryWrapper, List<String> cols, boolean distinct, Class<?> entityClazz) {
        if (cols == null || cols.isEmpty()) {
            return queryWrapper;
        }
        //查询字段
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
        return queryWrapper;
    }

    public static <T> QueryWrapper addConditions(QueryWrapper<T> queryWrapper, List<Cond> conds, Class<?> entityClazz) {
        if (conds == null || conds.isEmpty()) {
            return queryWrapper;
        }
        Consumer<QueryWrapper<T>> consumer = wrapperConsumer(conds, Rel.AND, entityClazz);
        consumer.accept(queryWrapper);
        return queryWrapper;
    }

    private static <T> Consumer<QueryWrapper<T>> wrapperConsumer(List<Cond> conds, Rel rel, Class<?> entityClazz) {
        return qw -> {
            for (int i = 0; i < conds.size(); i++) {
                Cond cond = conds.get(i);
                if (cond.getConds() != null && cond.getConds().size() > 0) {
                    qw.nested(wrapperConsumer(cond.getConds(), cond.getRel(), entityClazz));
                } else {
                    addCondition(qw, cond, rel, entityClazz);
                }
            }
        };
    }

    public static <T> QueryWrapper addOrders(QueryWrapper<T> queryWrapper, List<Order> orders, Class<?> entityClazz) {
        if (orders == null) {
            return queryWrapper;
        }
        for (Order order : orders) {
            if (order.isAsc()) {
                queryWrapper.orderByAsc(ColumnUtil.toDbField(order.getCol(), entityClazz));
            } else {
                queryWrapper.orderByDesc(ColumnUtil.toDbField(order.getCol(), entityClazz));
            }
        }
        return queryWrapper;
    }

    private static <T> QueryWrapper addCondition(QueryWrapper<T> queryWrapper, Cond cond, Rel rel, Class<?> entityClazz) {
        String field = cond.getCol();
        TableFieldInfo fieldInfo = EntityUtil.getTableFieldInfo(entityClazz, field);
        Assert.notNull(fieldInfo, "请检查字段是否正确：" + field + ", 并确保类(" + entityClazz.getName() + ")中含有此字段。");
        String dbField = ColumnUtil.wrapper(fieldInfo.getColumn());
        boolean effect = true; // 值是否有效
        if (cond.getVal() == null || (cond.getVal() instanceof CharSequence && StrUtil.isBlank((CharSequence) cond.getVal()))) {
            effect = false;
        }
        switch (cond.getOpt()) {
            case EQ:
                if (rel == Rel.AND) {
                    queryWrapper.eq(effect, dbField, cond.getVal());
                } else {
                    queryWrapper.or(effect, q -> q.eq(dbField, cond.getVal()));
                }
                break;
            case NE:
                if (rel == Rel.AND) {
                    queryWrapper.ne(effect, dbField, cond.getVal());
                } else {
                    queryWrapper.or(effect, q -> q.ne(dbField, cond.getVal()));
                }
                break;
            case GT:
                if (rel == Rel.AND) {
                    queryWrapper.gt(effect, dbField, cond.getVal());
                } else {
                    queryWrapper.or(effect, q -> q.gt(dbField, cond.getVal()));
                }
                break;
            case GE:
                if (rel == Rel.AND) {
                    queryWrapper.ge(effect, dbField, cond.getVal());
                } else {
                    queryWrapper.or(effect, q -> q.ge(dbField, cond.getVal()));
                }
                break;
            case LT:
                if (rel == Rel.AND) {
                    queryWrapper.lt(effect, dbField, cond.getVal());
                } else {
                    queryWrapper.or(effect, q -> q.lt(dbField, cond.getVal()));
                }
                break;
            case LE:
                if (rel == Rel.AND) {
                    queryWrapper.le(effect, dbField, cond.getVal());
                } else {
                    queryWrapper.or(effect, q -> q.le(dbField, cond.getVal()));
                }
                break;
            case IN:
                if (rel == Rel.AND) {
                    queryWrapper.in(effect, dbField, (Collection) cond.getVal());
                } else {
                    queryWrapper.or(effect, q -> q.in(dbField, cond.getVal()));
                }
                break;
            case NIN:
                if (rel == Rel.AND) {
                    queryWrapper.notIn(effect, dbField, (Collection) cond.getVal());
                } else {
                    queryWrapper.or(effect, q -> q.notIn(dbField, cond.getVal()));
                }
                break;

            case LIKE:
                if (rel == Rel.AND) {
                    queryWrapper.like(effect, dbField, cond.getVal());
                } else {
                    queryWrapper.or(effect, q -> q.like(dbField, cond.getVal()));
                }
                break;
            case LLIKE:
                if (rel == Rel.AND) {
                    queryWrapper.likeLeft(effect, dbField, cond.getVal());
                } else {
                    queryWrapper.or(effect, q -> q.likeLeft(dbField, cond.getVal()));
                }
                break;
            case RLIKE:
                if (rel == Rel.AND) {
                    queryWrapper.likeRight(effect, dbField, cond.getVal());
                } else {
                    queryWrapper.or(effect, q -> q.likeRight(dbField, cond.getVal()));
                }
                break;
            case NLIKE:
                if (rel == Rel.AND) {
                    queryWrapper.notLike(effect, dbField, cond.getVal());
                } else {
                    queryWrapper.or(effect, q -> q.notLike(dbField, cond.getVal()));
                }
                break;
            case NULL:
                if (rel == Rel.AND) {
                    queryWrapper.isNull(dbField);
                } else {
                    queryWrapper.or(q -> q.isNull(dbField));
                }
                break;
            case NNULL:
                if (rel == Rel.AND) {
                    queryWrapper.isNotNull(dbField);
                } else {
                    queryWrapper.or(q -> q.isNotNull(dbField));
                }
                break;
            case EMPTY:
                if (rel == Rel.AND) {
                    queryWrapper.nested(q -> q.isNull(dbField).or().eq(fieldInfo.isCharSequence(), dbField, ""));
                } else {
                    queryWrapper.or(q -> q.nested(n -> n.isNull(dbField).or().eq(fieldInfo.isCharSequence(), dbField, "")));
                }
                break;
            case NEMPTY:
                if (rel == Rel.AND) {
                    queryWrapper.nested(q -> q.isNotNull(dbField).ne(fieldInfo.isCharSequence(), dbField, ""));
                } else {
                    queryWrapper.or(q -> q.nested(n -> n.isNotNull(dbField).ne(fieldInfo.isCharSequence(), dbField, "")));
                }
                break;
        }
        return queryWrapper;
    }
}