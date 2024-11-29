package io.github.pengxianggui.crud;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;

public abstract class BaseServiceImpl<T, M extends BaseMapper<T>> extends ServiceImpl<M, T> implements BaseService<T> {
    protected Class<T> clazz;

    @PostConstruct
    public abstract void init();

    @Override
    public boolean updateById(UpdateModelWrapper<T> modelWrapper) {
        Assert.isTrue(modelWrapper.getPkVal() != null, "[%s]主键不能为空", modelWrapper.getPkName());
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(modelWrapper.getPkName(), modelWrapper.getPkVal());
        T model = modelWrapper.getModel();
        Field[] fields = model.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            if (fieldName.equals(modelWrapper.getPkName())) {
                continue;
            }
            try {
                Object fieldValue = field.get(model);
                if (fieldValue == null && modelWrapper.isUpdateNull() == Boolean.FALSE) {
                    continue;
                }
                updateWrapper.set(fieldName, fieldValue);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return this.update(updateWrapper);
    }

}