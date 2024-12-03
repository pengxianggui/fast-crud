package io.github.pengxianggui.crud;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.pengxianggui.crud.meta.EntityUtil;
import io.github.pengxianggui.crud.query.Cond;
import io.github.pengxianggui.crud.query.QueryWrapperUtil;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.List;

public abstract class BaseServiceImpl<T, M extends BaseMapper<T>> extends ServiceImpl<M, T> implements BaseService<T> {
    protected Class<T> clazz;

    @PostConstruct
    public abstract void init();

    @Override
    public boolean updateById(UpdateModelWrapper<T> modelWrapper) {
        if (modelWrapper.get_updateNull() == null) {
            return updateById(modelWrapper.getModel());
        }

        Assert.isTrue(EntityUtil.getPkVal(modelWrapper.getModel()) != null,
                "[%s]主键不能为空", EntityUtil.getPkName(modelWrapper.getModel()));
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(EntityUtil.getPkName(modelWrapper.getModel()), EntityUtil.getPkVal(modelWrapper.getModel()));
        T model = modelWrapper.getModel();
        Field[] fields = model.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            if (fieldName.equals(EntityUtil.getPkName(modelWrapper.getModel()))) {
                continue;
            }
            try {
                Object fieldValue = field.get(model);
                if (fieldNeedUpdate(field, fieldValue, modelWrapper.get_updateNull())) {
                    updateWrapper.set(EntityUtil.getDbFieldName(modelWrapper.getModel(), fieldName), fieldValue);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return this.update(updateWrapper);
    }

    public boolean exists(List<Cond> conditions) {
        QueryWrapper<T> wrapper = new QueryWrapper<T>();
        QueryWrapperUtil.addConditions(wrapper, conditions);
        wrapper.last(" limit 1");
        return this.count(wrapper) > 0;
    }

    /**
     * 判断字段是否需要更新，通过mybatisplus的@TableField注解判断
     *
     * @param field      字段
     * @param fieldValue 字段值
     * @param updateNull 是否更新null值，优先级低于@TableField注解中的更新策略
     * @return
     */
    private boolean fieldNeedUpdate(Field field, Object fieldValue, boolean updateNull) {
        boolean defaultPredicate = (fieldValue != null || updateNull); // 默认判断条件
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField == null) { // 无@TableField修饰，则判断依据交给入参updateNull
            return defaultPredicate;
        }
        boolean exist = tableField.exist();
        if (exist == Boolean.FALSE) { // 针对不存在的字段, 直接返回false
            return false;
        }
        // 其它情况由更新策略和入参updateNull组合判断，更新策略优先级高于入参updateNull
        FieldStrategy updateStrategy = tableField.updateStrategy();
        switch (updateStrategy) {
            case IGNORED:
            case ALWAYS:
                return true;
            case NOT_NULL:
                return fieldValue != null;
            case NOT_EMPTY:
                return (fieldValue instanceof CharSequence) ? StrUtil.isNotBlank((CharSequence) fieldValue) : fieldValue != null;
            case NEVER:
                return false;
            case DEFAULT:
            default:
                return defaultPredicate;
        }
    }

}