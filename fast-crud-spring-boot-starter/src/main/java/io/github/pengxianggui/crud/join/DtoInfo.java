package io.github.pengxianggui.crud.join;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.google.common.collect.ImmutableList;
import io.github.pengxianggui.crud.query.Opt;
import io.github.pengxianggui.crud.util.EntityUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dto解析
 *
 * @author pengxg
 * @date 2025/5/23 13:41
 */
@NoArgsConstructor
@Getter
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
     * join信息，包括三种join
     */
    private List<JoinInfo> joinInfos;
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
        List<JoinInfo> joinInfos = new ArrayList<>();
        List<JoinInfo> innerJoinInfo = Arrays.stream(dtoClazz.getAnnotationsByType(InnerJoin.class))
                .map(join -> new JoinInfo(this, JoinType.INNER, join.value(), join.alias(), join.on(), join.readonly()))
                .collect(Collectors.toList());
        joinInfos.addAll(innerJoinInfo);
        List<JoinInfo> leftJoinInfo = Arrays.stream(dtoClazz.getAnnotationsByType(LeftJoin.class))
                .map(join -> new JoinInfo(this, JoinType.LEFT, join.value(), join.alias(), join.on(), join.readonly()))
                .collect(Collectors.toList());
        joinInfos.addAll(leftJoinInfo);
        List<JoinInfo> rightJoinInfo = Arrays.stream(dtoClazz.getAnnotationsByType(RightJoin.class))
                .map(join -> new JoinInfo(this, JoinType.RIGHT, join.value(), join.alias(), join.on(), join.readonly()))
                .collect(Collectors.toList());
        joinInfos.addAll(rightJoinInfo);
        this.joinInfos = ImmutableList.copyOf(joinInfos);
        List<DtoField> fields = Arrays.stream(ReflectUtil.getFields(dtoClazz)).filter(field -> !EntityUtil.isMarkAsNotDbField(field))
                .map(field -> new DtoField(this, field)).collect(Collectors.toList());
        this.fields = ImmutableList.copyOf(fields);
    }

    /**
     * 根据字段名获取dto字段信息
     *
     * @param col
     * @return
     */
    DtoField getField(String col) {
        for (DtoField dtoField : fields) {
            if (dtoField.getField().getName().equals(col)) {
                return dtoField;
            }
        }
        return null;
    }

    /**
     * 根据dto字段获取其所属的join类。
     *
     * @param field
     * @return 如果所属主类，则返回null
     */
    JoinInfo getJoinInfo(DtoField field) {
        Optional<JoinInfo> joinInfo = this.joinInfos.stream()
                .filter(j -> j.getJoinEntityClass().equals(field.getTargetClazz()) && Objects.equals(j.getAlias(), field.alias))
                .findFirst();
        return joinInfo.orElse(null);
    }

    @NoArgsConstructor
    @Getter
    static class JoinInfo {
        private DtoInfo dtoInfo;
        private JoinType joinType;
        private Class<?> joinEntityClass;
        private String alias;
        private Class<?> targetEntityClass;
        private OnCondition[] condFieldRelates;
        private boolean readonly;

        /**
         * @param dtoInfo         dtoInfo
         * @param joinType        join类型
         * @param joinEntityClass join的entity类
         * @param alias           join的别名
         * @param onConds         on条件
         */
        JoinInfo(DtoInfo dtoInfo, JoinType joinType, Class<?> joinEntityClass, String alias, OnCond[] onConds, boolean readonly) {
            Assert.isTrue(onConds.length > 0,
                    "There must be at least one @OnCond in Class: {}", dtoInfo.dtoClazz.getName());
            this.dtoInfo = dtoInfo;
            this.joinType = joinType;
            this.joinEntityClass = joinEntityClass;
            this.alias = StrUtil.blankToDefault(alias, null); // 空串时必须转为null, MPJ才会使用默认别名
            this.readonly = readonly;
            this.targetEntityClass = onConds[0].targetClazz() == Void.class ? dtoInfo.mainEntityClazz : onConds[0].targetClazz();
            this.condFieldRelates = new OnCondition[onConds.length];
            for (int i = 0; i < onConds.length; i++) {
                OnCondition condFieldRelate = new OnCondition(this, onConds[i]);
                condFieldRelates[i] = condFieldRelate;
            }
        }
    }

    @NoArgsConstructor
    static class DtoField {
        /**
         * 所属DtoInfo,持有上层引用
         */
        protected DtoInfo dtoInfo;
        /**
         * dto类
         */
        protected Class<?> dtoClazz;
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
         * 关联实体类对应表的别名
         */
        @Getter
        protected String alias;
        /**
         * 关联的实体字段(可能为null)
         */
        @Getter
        protected Field targetField;

        /**
         * field是否映射一个数据库字段
         */
        protected boolean isDbField;

        /**
         * @param field dto字段
         */
        DtoField(DtoInfo dtoInfo, Field field) {
            this.dtoInfo = dtoInfo;
            this.dtoClazz = dtoInfo.dtoClazz;
            this.field = field;
            String targetFieldName = field.getName();
            if (field.isAnnotationPresent(RelateTo.class)) {
                RelateTo relateTo = field.getAnnotation(RelateTo.class);
                this.targetClazz = relateTo.value();
                this.alias = StrUtil.blankToDefault(relateTo.alias(), null);
                this.isDbField = relateTo.dbField();
                targetFieldName = StrUtil.blankToDefault(relateTo.field(), targetFieldName);
            } else {
                this.targetClazz = dtoInfo.mainEntityClazz;
                this.isDbField = !EntityUtil.isMarkAsNotDbField(field);
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
            return MethodReferenceRegistry.getFunction(this.dtoClazz, field);
        }

        /**
         * 获取关联的实体类中字段Getter Method Reference
         *
         * @param <T>
         * @param <R>
         * @return
         */
        <T, R> SFunction<T, R> getTargetFieldGetter() {
            if (targetField == null) {
                throw new ClassJoinParseException(field.getDeclaringClass(),
                        "The relate target field of [{}] in class [{}] is not found, you may add @JoinIgnore", field.getName(), field.getDeclaringClass());
            }
            return MethodReferenceRegistry.getFunction(this.targetClazz, targetField);
        }

        /**
         * 判断dto中此字段是否标注@JoinIgnore
         *
         * @return
         */
        private boolean isJoinIgnore() {
            return this.field.isAnnotationPresent(JoinIgnore.class);
        }

        /**
         * 判断dto中此字段是否查询时忽略
         *
         * @return
         */
        public boolean ignoreForQuery() {
            return isJoinIgnore()
                    && Arrays.stream(this.field.getAnnotation(JoinIgnore.class).value())
                    .anyMatch(ignoreWhen -> ignoreWhen == IgnoreWhen.Query);
        }

        /**
         * 判断dto中此字段是否更新时忽略
         *
         * @return
         */
        public boolean ignoreForUpdate() {
            boolean ignore = isJoinIgnore()
                    && Arrays.stream(this.field.getAnnotation(JoinIgnore.class).value())
                    .anyMatch(ignoreWhen -> ignoreWhen == IgnoreWhen.Update);
            if (ignore) {
                return true;
            }
            JoinInfo joinInfo = this.dtoInfo.getJoinInfo(this);
            return joinInfo != null && joinInfo.isReadonly();
        }

        /**
         * 判断dto中此字段是否插入时忽略
         *
         * @return
         */
        public boolean ignoreForInsert() {
            boolean ignore = isJoinIgnore()
                    && Arrays.stream(this.field.getAnnotation(JoinIgnore.class).value())
                    .anyMatch(ignoreWhen -> ignoreWhen == IgnoreWhen.Insert);
            if (ignore) {
                return true;
            }
            JoinInfo joinInfo = this.dtoInfo.getJoinInfo(this);
            return joinInfo != null && joinInfo.isReadonly();
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

        public boolean isDbField() {
            return isDbField;
        }

        /**
         * 将dto中的字段值复制给entity中的字段值
         *
         * @param model
         * @param entity
         */
        public void copyValue(Object model, Object entity) {
            if (this.targetFieldNotExist()) {
                return;
            }
            Class dtoClazz = this.field.getDeclaringClass();
            if (!dtoClazz.isInstance(model)) {
                throw new ClassJoinParseException(dtoClazz, "The model [{}] is not instance of [{}]", model, dtoClazz);
            }
            ReflectUtil.setFieldValue(entity, this.targetField, getFieldGetter().apply(model));
        }
    }

    @NoArgsConstructor
    @Getter
    static class OnCondition {
        private Class<?> clazz;
        protected String alias;
        private Field field;
        private Opt opt;
        // 常量值
        private String constVal;
        // 关联目标字段
        protected Class<?> targetClazz;
        protected String rightAlias;
        protected Field targetField;

        public OnCondition(JoinInfo joinInfo, OnCond onCond) {
            this.clazz = joinInfo.getJoinEntityClass();
            this.field = ReflectUtil.getField(this.clazz, onCond.field());
            this.opt = onCond.opt();
            if (StrUtil.isNotBlank(onCond.constVal())) {
                this.constVal = onCond.constVal();
            } else {
                this.targetClazz = joinInfo.getTargetEntityClass();
                this.targetField = ReflectUtil.getField(this.targetClazz, StrUtil.blankToDefault(onCond.targetField(), onCond.field()));
                this.alias = StrUtil.firstNonBlank(onCond.alias(), joinInfo.alias);
                this.rightAlias = StrUtil.blankToDefault(onCond.targetAlias(), null);
            }
        }

        public <T, R> SFunction<T, R> getFieldGetter() {
            return MethodReferenceRegistry.getFunction(this.clazz, field);
        }

        /**
         * 是否常量(条件)
         *
         * @return
         */
        public boolean isConst() {
            return StrUtil.isNotBlank(this.constVal);
        }

        public <T, R> SFunction<T, R> getTargetFieldGetter() {
            return MethodReferenceRegistry.getFunction(this.targetClazz, targetField);
        }
    }

    enum JoinType {
        INNER,
        LEFT,
        RIGHT
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
