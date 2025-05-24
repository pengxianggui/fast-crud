package io.github.pengxianggui.crud.query.join;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.yulichang.wrapper.JoinAbstractLambdaWrapper;
import io.github.pengxianggui.crud.query.Opt;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * dto解析
 *
 * @author pengxg
 * @date 2025/5/23 13:41
 */
@NoArgsConstructor
@Data
public class DtoInfo<DTO, T> {
    /**
     * dto类
     */
    private Class<DTO> dtoClass;
    /**
     * 主表类
     */
    private Class<T> mainEntityClass;
    /**
     * 关联表信息
     */
    private Map<JoinType, List<JoinInfo<T>>> joinMap;
    /**
     * dto类中的字段信息
     */
    private List<FieldRelate> fields;

    public DtoInfo(Class<DTO> dtoClass, Class<T> clazz) {
        this.dtoClass = dtoClass;
        this.mainEntityClass = clazz;
        Join[] joins = dtoClass.getAnnotationsByType(Join.class);
        this.joinMap = Arrays.stream(joins).map(join -> new JoinInfo<>(join, clazz))
                .collect(Collectors.groupingBy(joinInfo -> joinInfo.getJoin().joinType()));
        this.fields = Arrays.stream(ReflectUtil.getFields(dtoClass))
                .map(field -> new FieldRelate(field, clazz, dtoClass)).collect(Collectors.toList());
    }

    public FieldRelate getField(String col) {
        for (FieldRelate fieldRelate : fields) {
            if (fieldRelate.getField().getName().equals(col)) {
                return fieldRelate;
            }
        }
        return null;
    }

    @NoArgsConstructor
    @Getter
    public static class JoinInfo<T> {
        private Join join;
        private CondFieldRelate[] condFieldRelates;

        public JoinInfo(Join join, Class<T> clazz) {
            this.join = join;
            this.condFieldRelates = new CondFieldRelate[join.on().length];
            for (int i = 0; i < join.on().length; i++) {
                CondFieldRelate condFieldRelate = new CondFieldRelate(clazz, join.value(), join.on()[i]);
                condFieldRelates[i] = condFieldRelate;
            }
        }

        public JoinAbstractLambdaWrapper<T, ?> apply(JoinAbstractLambdaWrapper<T, ?> on) {
            for (CondFieldRelate condFieldRelate : this.condFieldRelates) {
                SFunction<T, ?> fieldGetter = condFieldRelate.getFieldGetter();
                SFunction<T, ?> targetFieldGetter = condFieldRelate.getTargetFieldGetter();
                switch (condFieldRelate.opt) {
                    case EQ:
                        on.eq(targetFieldGetter, fieldGetter);
                        break;
                    case NE:
                        on.ne(targetFieldGetter, fieldGetter);
                        break;
                    case GT:
                        on.gt(targetFieldGetter, fieldGetter);
                        break;
                    case GE:
                        on.ge(targetFieldGetter, fieldGetter);
                        break;
                    case LT:
                        on.lt(targetFieldGetter, fieldGetter);
                        break;
                    case LE:
                        on.le(targetFieldGetter, fieldGetter);
                        break;
                    case IN:
                        on.in(targetFieldGetter, fieldGetter);
                        break;
                    case NIN:
                        on.notIn(targetFieldGetter, fieldGetter);
                        break;
                    case LIKE:
                        on.like(targetFieldGetter, fieldGetter);
                        break;
                    case NLIKE:
                        on.notLike(targetFieldGetter, fieldGetter);
                        break;
                    case NULL:
                        on.isNull(targetFieldGetter);
                        break;
                    case NNULL:
                        on.isNotNull(targetFieldGetter);
                        break;
                    case EMPTY:
                        on.nested(q -> q.isNull(targetFieldGetter).or().eq(targetFieldGetter, ""));
                        break;
                    case NEMPTY:
                        on.nested(q -> q.isNotNull(targetFieldGetter).ne(targetFieldGetter, ""));
                        break;
                }
            }
            return on;
        }
    }

    @NoArgsConstructor
    @Getter
    public static class FieldRelate<T> {
        protected Field field;
        protected Field targetField;

        public FieldRelate(Field field, Class<T> clazz, Class<?> dtoClazz) {
            this.field = field;
            if (field.isAnnotationPresent(RelateTo.class)) {
                RelateTo relateTo = field.getAnnotation(RelateTo.class);
                ReflectUtil.getField(dtoClazz, StrUtil.blankToDefault(relateTo.field(), field.getName()));
            } else {
                this.targetField = field;
            }
        }

        public <E> SFunction<E, ?> getFieldGetter() {
            return getFieldGetterFunction(field);
            // TODO 改为从注册中心获取，注册中心采用APT技术生成
        }

        public <E> SFunction<E, ?> getTargetFieldGetter() {
            return getFieldGetterFunction(targetField);
            // TODO 改为从注册中心获取，注册中心采用APT技术生成
        }
    }

    @NoArgsConstructor
    @Getter
    public static class CondFieldRelate extends FieldRelate {
        private Opt opt;

        public CondFieldRelate(Class<?> clazz, Class<?> targetClazz, OnCond onCond) {
            this.opt = onCond.opt();
            this.field = ReflectUtil.getField(clazz, onCond.field());
            this.targetField = ReflectUtil.getField(targetClazz, onCond.targetField());
        }
    }

    /**
     * 获取字段的getter方法并返回，以便提供外部作为给MPJ的lambda参数。例如.selectAs(getFieldGetterFunction(field1), getFieldGetterFunction(field2))
     * 此招不通！MPJ在解析lambda表达式时使用SerializedLambda推断字段名，这里返回的是运行时反射构造的lambda表达式(匿名内部类), MPJ不支持, 会报错:
     * org.apache.ibatis.reflection.ReflectionException: Error parsing property name 'lambda$getFieldGetterFunction$6d075322$1'.  Didn't start with 'is', 'get' or 'set'.
     * gpt明确给出结论: 我们是无法通过运行时构造lambda表达式传入 .selectAs(lambdaA, lambdaB), 无法识别的。
     * 难搞！但是gpt也指了一条活路: 那就是利用编译器代码生成技术, Java APT（Annotation Processing Tool）
     *
     * @param field
     * @param <E>
     * @return
     * @deprecated
     */
    @Deprecated
    private static <E> SFunction<E, ?> getFieldGetterFunction(Field field) {
        return (SFunction<E, Object>) e -> {
            field.setAccessible(Boolean.TRUE);
            try {
                return field.get(e);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}
