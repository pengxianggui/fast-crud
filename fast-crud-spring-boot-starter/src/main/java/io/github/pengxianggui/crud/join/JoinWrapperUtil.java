package io.github.pengxianggui.crud.join;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.yulichang.wrapper.JoinAbstractLambdaWrapper;
import com.github.yulichang.wrapper.JoinAbstractWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.github.yulichang.wrapper.UpdateJoinWrapper;
import io.github.pengxianggui.crud.config.MapperResolver;
import io.github.pengxianggui.crud.query.*;
import io.github.pengxianggui.crud.util.EntityUtil;
import io.github.pengxianggui.crud.util.TableFieldInfoWrapper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
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
        Assert.equals(dtoInfo.getMainEntityClazz(), clazz,
                "Type inconsistency: The main type declared in the dto is inconsistent");
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

    static <T> void addSelect(MPJLambdaWrapper<T> wrapper, List<String> cols, @Nullable Boolean distinct, DtoInfo dtoInfo) {
        List<DtoInfo.DtoField> dtoFields = dtoInfo.getFields();
        dtoFields.stream().filter(field -> cols == null || cols.size() == 0 || cols.contains(field.getField().getName()))
                .forEach(field -> {
                    if (field.ignoreForQuery()) {
                        return;
                    }

                    if (field.isDbField()) { // 无论是什么类型(简单类型、集合、对象类型),标记为数据库字段则直接select
                        addSelectAs(wrapper, field);
                        return;
                    }

                    if (Collection.class.isAssignableFrom(field.getField().getType())) { // 一对多
                        Type type = TypeUtil.getTypeArgument(field.getField().getGenericType());
                        if (ClassUtil.isSimpleValueType(TypeUtil.getClass(type))) { // 单字段
                            wrapper.selectCollection(field.getAlias(), field.getTargetClazz(), field.getFieldGetter(), map -> {
                                map.result(field.getTargetFieldGetter());
                                return map;
                            });
                        } else {
                            if (field.getTargetClazz() == type) {
                                wrapper.selectCollection(field.getAlias(), field.getTargetClazz(), field.getFieldGetter());
                            } else {
                                wrapper.selectCollection(field.getAlias(), field.getTargetClazz(), field.getFieldGetter(), map -> {
                                    DtoInfo subDtoInfo = new DtoInfo(TypeUtil.getClass(type));
                                    // 这里面暂不考虑递归情况, 否则太复杂, 还要牵扯子查询, mybatis-plus-join怕无法支撑
                                    subDtoInfo.getFields().forEach(field1 -> {
                                        if (field1.ignoreForQuery() || field1.targetFieldNotExist()) {
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
                            addSelectAs(wrapper, field);
                        } else { // 一对一映射实体
                            Type type = field.getField().getType();
                            wrapper.selectAssociation(field.getAlias(), field.getTargetClazz(), field.getFieldGetter(), map -> {
                                DtoInfo subDtoInfo = new DtoInfo(TypeUtil.getClass(type), field.getTargetClazz());
                                // 这里面暂不考虑递归情况, 否则太复杂, 还要牵扯子查询, mybatis-plus-join怕无法支撑
                                subDtoInfo.getFields().forEach(field1 -> {
                                    if (field1.ignoreForQuery()) {
                                        return;
                                    }
                                    map.result(field1.getTargetFieldGetter(), field1.getFieldGetter());
                                });
                                return map;
                            });
                        }
                    }
                });
        if (distinct == Boolean.TRUE) {
            wrapper.distinct();
        }
    }

    static <T> void addJoin(JoinAbstractLambdaWrapper<T, ? extends JoinAbstractLambdaWrapper<T, ?>> wrapper,
                            DtoInfo dtoInfo) {
        Assert.equals(dtoInfo.getMainEntityClazz(), wrapper.getEntityClass(),
                "Type inconsistency: The main type declared in the dto is inconsistent");
        dtoInfo.getJoinInfos().forEach(join -> {
            switch (join.getJoinType()) {
                case INNER:
                    wrapper.innerJoin(join.getJoinEntityClass(), join.getAlias(), on -> {
                        addJoinConditions(on, join.getCondFieldRelates());
                        return on;
                    });
                    break;
                case LEFT:
                    wrapper.leftJoin(join.getJoinEntityClass(), join.getAlias(), on -> {
                        addJoinConditions(on, join.getCondFieldRelates());
                        return on;
                    });
                    break;
                case RIGHT:
                    wrapper.rightJoin(join.getJoinEntityClass(), join.getAlias(), on -> {
                        addJoinConditions(on, join.getCondFieldRelates());
                        return on;
                    });
                    break;
            }
        });
    }

    /**
     * 从dtoClazz中解析join信息，配置到wrapper中
     *
     * @param wrapper
     * @param dtoClazz
     * @param <T>
     */
    public static <T> void addJoin(JoinAbstractLambdaWrapper<T, ? extends JoinAbstractLambdaWrapper<T, ?>> wrapper, Class<?> dtoClazz) {
        DtoInfo dtoInfo = getDtoInfo(dtoClazz);
        addJoin(wrapper, dtoInfo);
    }

    static <T, C extends JoinAbstractWrapper<T, C>> void addJoinConditions(JoinAbstractWrapper<T, C> on,
                                                                           DtoInfo.OnCondition[] condFieldRelates) {
        for (DtoInfo.OnCondition condFieldRelate : condFieldRelates) {
            Opt opt = condFieldRelate.getOpt();
            SFunction<?, ?> fieldGetter = condFieldRelate.getFieldGetter();
            String alias = condFieldRelate.getAlias();
            if (condFieldRelate.isConst()
                    || opt == Opt.EMPTY
                    || opt == Opt.NULL
                    || opt == Opt.NNULL
                    || opt == Opt.NEMPTY) {
                String constVal = condFieldRelate.getConstVal();
                switch (opt) {
                    case EQ:
                        on.eq(alias, fieldGetter, constVal);
                        break;
                    case NE:
                        on.ne(alias, fieldGetter, constVal);
                        break;
                    case GT:
                        on.gt(alias, fieldGetter, constVal);
                        break;
                    case GE:
                        on.ge(alias, fieldGetter, constVal);
                        break;
                    case LT:
                        on.lt(alias, fieldGetter, constVal);
                        break;
                    case LE:
                        on.le(alias, fieldGetter, constVal);
                        break;
                    case IN:
                        on.in(alias, fieldGetter, constVal);
                        break;
                    case NIN:
                        on.notIn(alias, fieldGetter, constVal);
                        break;
                    case LIKE:
                        on.like(alias, fieldGetter, constVal);
                        break;
                    case LLIKE:
                        on.likeLeft(alias, fieldGetter, constVal);
                        break;
                    case RLIKE:
                        on.likeRight(alias, fieldGetter, constVal);
                        break;
                    case NLIKE:
                        on.notLike(alias, fieldGetter, constVal);
                        break;
                    case NULL:
                        on.isNull(alias, fieldGetter);
                        break;
                    case NNULL:
                        on.isNotNull(alias, fieldGetter);
                        break;
                    case EMPTY:
                        TableFieldInfoWrapper fieldInfo = EntityUtil.getTableFieldInfo(condFieldRelate.getClazz(), condFieldRelate.getField().getName());
                        Assert.notNull(fieldInfo, "请检查字段是否正确：" + condFieldRelate.getField().getName() + ", 并确保类(" + condFieldRelate.getClazz() + ")中含有此字段。");
                        on.nested(q -> q.isNull(alias, fieldGetter).or().eq(fieldInfo.isCharSequence(), alias, fieldGetter, ""));
                        break;
                    case NEMPTY:
                        TableFieldInfoWrapper fieldInfo1 = EntityUtil.getTableFieldInfo(condFieldRelate.getClazz(), condFieldRelate.getField().getName());
                        Assert.notNull(fieldInfo1, "请检查字段是否正确：" + condFieldRelate.getField().getName() + ", 并确保类(" + condFieldRelate.getClazz() + ")中含有此字段。");
                        on.nested(q -> q.isNotNull(alias, fieldGetter).ne(fieldInfo1.isCharSequence(), alias, fieldGetter, ""));
                        break;
                }
            } else {
                SFunction<?, ?> targetFieldGetter = condFieldRelate.getTargetFieldGetter();
                String targetAlias = condFieldRelate.getRightAlias();
                switch (opt) {
                    case EQ:
                        on.eq(alias, fieldGetter, targetAlias, targetFieldGetter);
                        break;
                    case NE:
                        on.ne(alias, fieldGetter, targetAlias, targetFieldGetter);
                        break;
                    case GT:
                        on.gt(alias, fieldGetter, targetAlias, targetFieldGetter);
                        break;
                    case GE:
                        on.ge(alias, fieldGetter, targetAlias, targetFieldGetter);
                        break;
                    case LT:
                        on.lt(alias, fieldGetter, targetAlias, targetFieldGetter);
                        break;
                    case LE:
                        on.le(alias, fieldGetter, targetAlias, targetFieldGetter);
                        break;
                    case IN:
                        on.in(alias, fieldGetter, targetAlias, targetFieldGetter);
                        break;
                    case NIN:
                        on.notIn(alias, fieldGetter, targetAlias, targetFieldGetter);
                        break;
                    case LIKE:
                        on.like(alias, fieldGetter, targetFieldGetter);
                        break;
                    case LLIKE:
                        on.likeLeft(alias, fieldGetter, targetFieldGetter);
                        break;
                    case RLIKE:
                        on.likeRight(alias, fieldGetter, targetFieldGetter);
                        break;
                    case NLIKE:
                        on.notLike(alias, fieldGetter, targetFieldGetter);
                        break;
                }
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
        if (dtoField == null) {
            throw new IllegalArgumentException(cond.getCol() + "必须是dto的属性:" + dtoInfo.getDtoClazz());
        }
        if (dtoField.ignoreForQuery()) {
            return;
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
                    wrapper.eq(effect, dtoField.alias, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.eq(dtoField.alias, targetFieldGetter, cond.getVal()));
                }
                break;
            case NE:
                if (rel == Rel.AND) {
                    wrapper.ne(effect, dtoField.alias, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.ne(dtoField.alias, targetFieldGetter, cond.getVal()));
                }
                break;
            case GT:
                if (rel == Rel.AND) {
                    wrapper.gt(effect, dtoField.alias, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.gt(dtoField.alias, targetFieldGetter, cond.getVal()));
                }
                break;
            case GE:
                if (rel == Rel.AND) {
                    wrapper.ge(effect, dtoField.alias, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.ge(dtoField.alias, targetFieldGetter, cond.getVal()));
                }
                break;
            case LT:
                if (rel == Rel.AND) {
                    wrapper.lt(effect, dtoField.alias, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.lt(dtoField.alias, targetFieldGetter, cond.getVal()));
                }
                break;
            case LE:
                if (rel == Rel.AND) {
                    wrapper.le(effect, dtoField.alias, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.le(dtoField.alias, targetFieldGetter, cond.getVal()));
                }
                break;
            case IN:
                if (rel == Rel.AND) {
                    wrapper.in(effect, dtoField.alias, targetFieldGetter, (Collection) cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.in(dtoField.alias, targetFieldGetter, cond.getVal()));
                }
                break;
            case NIN:
                if (rel == Rel.AND) {
                    wrapper.notIn(effect, dtoField.alias, targetFieldGetter, (Collection) cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.notIn(dtoField.alias, targetFieldGetter, cond.getVal()));
                }
                break;
            case LIKE:
                if (rel == Rel.AND) {
                    wrapper.like(effect, dtoField.alias, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.like(dtoField.alias, targetFieldGetter, cond.getVal()));
                }
                break;
            case LLIKE:
                if (rel == Rel.AND) {
                    wrapper.likeLeft(effect, dtoField.alias, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.likeLeft(dtoField.alias, targetFieldGetter, cond.getVal()));
                }
                break;
            case RLIKE:
                if (rel == Rel.AND) {
                    wrapper.likeRight(effect, dtoField.alias, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.likeRight(dtoField.alias, targetFieldGetter, cond.getVal()));
                }
                break;
            case NLIKE:
                if (rel == Rel.AND) {
                    wrapper.notLike(effect, dtoField.alias, targetFieldGetter, cond.getVal());
                } else {
                    wrapper.or(effect, q -> q.notLike(dtoField.alias, targetFieldGetter, cond.getVal()));
                }
                break;
            case NULL:
                if (rel == Rel.AND) {
                    wrapper.isNull(dtoField.alias, targetFieldGetter);
                } else {
                    wrapper.or(q -> q.isNull(dtoField.alias, targetFieldGetter));
                }
                break;
            case NNULL:
                if (rel == Rel.AND) {
                    wrapper.isNotNull(dtoField.alias, targetFieldGetter);
                } else {
                    wrapper.or(q -> q.isNotNull(dtoField.alias, targetFieldGetter));
                }
                break;
            case EMPTY:
                TableFieldInfoWrapper fieldInfo = EntityUtil.getTableFieldInfo(dtoField.getTargetClazz(), dtoField.getTargetField().getName());
                Assert.notNull(fieldInfo, "请检查字段是否正确：" + dtoField.getTargetField().getName() + ", 并确保类(" + dtoField.getTargetClazz() + ")中含有此字段。");
                if (rel == Rel.AND) {
                    wrapper.nested(q -> q.isNull(dtoField.alias, targetFieldGetter).or().eq(fieldInfo.isCharSequence(), dtoField.alias, targetFieldGetter, ""));
                } else {
                    wrapper.or(q -> q.nested(n -> n.isNull(dtoField.alias, targetFieldGetter).or().eq(fieldInfo.isCharSequence(), dtoField.alias, targetFieldGetter, "")));
                }
                break;
            case NEMPTY:
                TableFieldInfoWrapper fieldInfo1 = EntityUtil.getTableFieldInfo(dtoField.getTargetClazz(), dtoField.getTargetField().getName());
                Assert.notNull(fieldInfo1, "请检查字段是否正确：" + dtoField.getTargetField().getName() + ", 并确保类(" + dtoField.getTargetClazz() + ")中含有此字段。");
                if (rel == Rel.AND) {
                    wrapper.nested(q -> q.isNotNull(dtoField.alias, targetFieldGetter).ne(fieldInfo1.isCharSequence(), dtoField.alias, targetFieldGetter, ""));
                } else {
                    wrapper.or(q -> q.nested(n -> n.isNotNull(dtoField.alias, targetFieldGetter).ne(fieldInfo1.isCharSequence(), dtoField.alias, targetFieldGetter, "")));
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
            if (dtoField == null) {
                throw new IllegalArgumentException(order.getCol() + "必须是dto的属性:" + dtoInfo.getDtoClazz());
            }
            if (dtoField.ignoreForQuery()) {
                continue;
            }
            if (order.isAsc()) {
                wrapper.orderByAsc(dtoField.alias, dtoField.getTargetFieldGetter());
            } else {
                wrapper.orderByDesc(dtoField.alias, dtoField.getTargetFieldGetter());
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
            if (field.ignoreForUpdate() || field.targetFieldNotExist() || field.isPk()) {
                continue;
            }

            try {
                field.getField().setAccessible(true);
                Object fieldValue = field.getField().get(dto);
                Field targetField = field.getTargetField();

                if (!EntityUtil.fieldNeedUpdate(targetField, fieldValue, updateNull)) {
                    continue;
                }
                // TODO 非常奇怪, UpdateJoinWrapper里没有支持alias的set重载方法，如果针对同一个表两次关联，分别set该如何？
                if (field.isDbField()) { // 无论是什么类型(简单类型、集合、对象类型),标记为数据库字段则直接set
                    String mapping = MapperResolver.getMapping(targetField);
                    if (StrUtil.isBlank(mapping)) {
                        wrapper.set(field.getTargetFieldGetter(), fieldValue);
                    } else {
                        wrapper.set(field.getTargetFieldGetter(), fieldValue, mapping);
                    }
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
                            if (subDtoInfoField.ignoreForUpdate() || subDtoInfoField.targetFieldNotExist()) {
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

    private static <T> void addSelectAs(MPJLambdaWrapper<T> wrapper, DtoInfo.DtoField field) {
        if (StrUtil.isNotBlank(field.getAlias())) { // 别名支持
            wrapper.selectAs(field.getAlias(), field.getTargetFieldGetter(), field.getFieldGetter());
        } else {
            wrapper.selectAs(field.getTargetFieldGetter(), field.getFieldGetter());
        }
    }
}
