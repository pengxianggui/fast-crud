package io.github.pengxianggui.crud.join;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.yulichang.wrapper.JoinAbstractLambdaWrapper;
import io.github.pengxianggui.crud.query.Opt;
import io.github.pengxianggui.crud.util.EntityUtil;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * dto解析
 *
 * @author pengxg
 * @date 2025/5/23 13:41
 */
@NoArgsConstructor
@Data
class DtoInfo {
    /**
     * dto类
     */
    private Class<?> dtoClazz;
    /**
     * 主表类
     */
    private Class<?> mainEntityClazz;
    /**
     * 内连信息
     */
    private List<JoinInfo> innerJoinInfo;
    /**
     * 左连信息
     */
    private List<JoinInfo> leftJoinInfo;
    /**
     * 右连信息
     */
    private List<JoinInfo> rightJoinInfo;
    /**
     * dto类中的字段信息
     */
    private List<DtoField> fields;

    /**
     * 解析dto类
     *
     * @param dtoClazz dto类
     */
    DtoInfo(Class<?> dtoClazz) {
        this(dtoClazz, Optional.ofNullable(dtoClazz.getAnnotation(JoinMain.class)).map(JoinMain::value)
                .orElseThrow(() -> new IllegalArgumentException(StrUtil.format("类{}必须被{}修饰", dtoClazz.getName(), JoinMain.class.getName()))));
    }

    /**
     * 解析dto类，并指定主实体类(覆盖可能的@JoinMain)
     *
     * @param dtoClazz        dto类
     * @param mainEntityClazz 主实体类
     */
    DtoInfo(Class<?> dtoClazz, Class<?> mainEntityClazz) {
        Assert.isTrue(dtoClazz != null, "dtoClazz不能为null");
        Assert.isTrue(mainEntityClazz != null, "mainEntityClazz不能为null");
        this.dtoClazz = dtoClazz;
        this.mainEntityClazz = mainEntityClazz;
        this.innerJoinInfo = Arrays.stream(dtoClazz.getAnnotationsByType(InnerJoin.class))
                .map(join -> new JoinInfo(this.mainEntityClazz, join.value(), join.on())).collect(Collectors.toList());
        this.leftJoinInfo = Arrays.stream(dtoClazz.getAnnotationsByType(LeftJoin.class))
                .map(join -> new JoinInfo(this.mainEntityClazz, join.value(), join.on())).collect(Collectors.toList());
        this.rightJoinInfo = Arrays.stream(dtoClazz.getAnnotationsByType(RightJoin.class))
                .map(join -> new JoinInfo(this.mainEntityClazz, join.value(), join.on())).collect(Collectors.toList());
        this.fields = Arrays.stream(ReflectUtil.getFields(dtoClazz))
                .map(field -> new DtoField(this.mainEntityClazz, field)).collect(Collectors.toList());
    }

    DtoField getField(String col) {
        for (DtoField dtoField : fields) {
            if (dtoField.getField().getName().equals(col)) {
                return dtoField;
            }
        }
        return null;
    }

    @NoArgsConstructor
    @Getter
    static class JoinInfo {
        private Class<?> mainEntityClass;
        private Class<?> targetEntityClass;
        private CondDtoField[] condFieldRelates;

        /**
         * @param mainEntityClass   主类
         * @param targetEntityClass join目标类
         * @param onConds           on条件
         */
        JoinInfo(Class<?> mainEntityClass, Class<?> targetEntityClass, OnCond[] onConds) {
            this.mainEntityClass = mainEntityClass;
            this.targetEntityClass = targetEntityClass;
            this.condFieldRelates = new CondDtoField[onConds.length];
            for (int i = 0; i < onConds.length; i++) {
                CondDtoField condFieldRelate = new CondDtoField(mainEntityClass, targetEntityClass, onConds[i]);
                condFieldRelates[i] = condFieldRelate;
            }
        }

        JoinAbstractLambdaWrapper apply(JoinAbstractLambdaWrapper<?, ?> on) {
            for (CondDtoField condFieldRelate : this.condFieldRelates) {
                // join左边entity中的cond字段的Getter Method Reference
                SFunction<?, ?> fieldGetter = condFieldRelate.getFieldGetter();
                // join右边entity中的cond字段的Getter Method Reference
                SFunction<?, ?> targetFieldGetter = condFieldRelate.getTargetFieldGetter();
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
    static class DtoField {
        /**
         * 属性
         */
        @Getter
        protected Field field;
        /**
         * 关联的实体类
         */
        @Getter
        protected Class<?> targetClazz;
        /**
         * 关联的实体字段(可能为null)
         */
        protected Field targetField;

        /**
         * @param entityClazz 字段field关联的目标entity类型，field上@RelateTo指定的优先
         * @param field       字段
         */
        DtoField(Class<?> entityClazz, Field field) {
            this.field = field;
            String targetFieldName = field.getName();
            if (field.isAnnotationPresent(RelateTo.class)) {
                RelateTo relateTo = field.getAnnotation(RelateTo.class);
                this.targetClazz = relateTo.value();
                targetFieldName = StrUtil.blankToDefault(relateTo.field(), targetFieldName);
            } else {
                this.targetClazz = entityClazz;
            }
            this.targetField = ReflectUtil.getField(this.targetClazz, targetFieldName);
        }

        /**
         * 获取dto中字段Getter Method Reference
         *
         * @param <T>
         * @param <R>
         * @return
         */
        <T, R> SFunction<T, R> getFieldGetter() {
            return MethodReferenceRegistry.getFunction(field);
        }

        /**
         * 获取关联的实体类中字段Getter Method Reference
         *
         * @param <T>
         * @param <R>
         * @return
         */
        <T, R> SFunction<T, R> getTargetFieldGetter() {
            return MethodReferenceRegistry.getFunction(targetField);
        }

        /**
         * 判断dto中此字段是否标注@JoinIgnore
         *
         * @return
         */
        public boolean isJoinIgnore() {
            return this.field.isAnnotationPresent(JoinIgnore.class);
        }

        /**
         * 判断dto中此字段是否标注@JoinIgnore并且指定查询时忽略
         *
         * @return
         */
        public boolean isJoinIgnoreForQuery() {
            return isJoinIgnore()
                    && Arrays.stream(this.field.getAnnotation(JoinIgnore.class).value())
                    .anyMatch(ignoreWhen -> ignoreWhen == IgnoreWhen.Query);
        }

        /**
         * 判断dto中此字段是否标注@JoinIgnore并且指定更新时忽略
         *
         * @return
         */
        public boolean isJoinIgnoreForUpdate() {
            return isJoinIgnore()
                    && Arrays.stream(this.field.getAnnotation(JoinIgnore.class).value())
                    .anyMatch(ignoreWhen -> ignoreWhen == IgnoreWhen.Update);
        }

        /**
         * 判断关联的目标字段是否存在
         *
         * @return
         */
        public boolean targetFieldNotExist() {
            return this.targetField == null;
        }

        /**
         * 判断关联的目标字段在实体类中是否为主键
         *
         * @return
         */
        public boolean isPk() {
            if (this.targetField == null) {
                return false;
            }
            return StrUtil.equals(EntityUtil.getPkName(this.targetClazz), this.targetField.getName());
        }
    }

    @NoArgsConstructor
    @Getter
    static class CondDtoField extends DtoField {
        private Opt opt;

        public CondDtoField(Class<?> mainEntityClass, Class<?> targetEntityClass, OnCond onCond) {
            this.opt = onCond.opt();
            this.field = ReflectUtil.getField(mainEntityClass, onCond.field());
            this.targetField = ReflectUtil.getField(targetEntityClass, StrUtil.blankToDefault(onCond.targetField(), onCond.field()));
        }

        /**
         * @return join左边entity中的cond字段
         */
        @Override
        public Field getField() {
            return field;
        }

        /**
         * 获取join左边entity中的cond字段的Getter Method Reference
         *
         * @param <T>
         * @param <R>
         * @return
         */
        @Override
        public <T, R> SFunction<T, R> getFieldGetter() {
            return super.getFieldGetter();
        }

        /**
         * 获取join右边entity中的cond字段的Getter Method Reference
         *
         * @param <T>
         * @param <R>
         * @return
         */
        @Override
        public <T, R> SFunction<T, R> getTargetFieldGetter() {
            return super.getTargetFieldGetter();
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
    static <E> SFunction<E, ?> getFieldGetterFunction(Field field) {
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
