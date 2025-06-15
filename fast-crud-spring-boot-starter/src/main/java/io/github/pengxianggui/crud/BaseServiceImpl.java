package io.github.pengxianggui.crud;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.github.yulichang.wrapper.UpdateJoinWrapper;
import io.github.pengxianggui.crud.dao.BaseMapper;
import io.github.pengxianggui.crud.file.FileManager;
import io.github.pengxianggui.crud.join.MPJLambdaWrapperBuilder;
import io.github.pengxianggui.crud.join.UpdateJoinWrapperBuilder;
import io.github.pengxianggui.crud.query.*;
import io.github.pengxianggui.crud.util.EntityUtil;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public abstract class BaseServiceImpl<T, M extends BaseMapper<T>> extends ServiceImpl<M, T> implements BaseService<T> {
    protected Class<T> clazz;

    @PostConstruct
    public abstract void init();

    @Resource
    private FileManager fileManager;

    @Override
    public List<T> queryList(Query query) {
        return list(QueryWrapperUtil.build(query, clazz));
    }

    @Override
    public Pager<T> queryPage(PagerQuery query) {
        Pager<T> pager = new Pager<>(query.getCurrent(), query.getSize());
        Wrapper<T> wrapper = QueryWrapperUtil.build(query, clazz);
        return page(pager, wrapper);
    }

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
        Query query = new Query();
        query.setConds(conditions);
        QueryWrapper<T> wrapper = QueryWrapperUtil.build(query, clazz);
        return this.count(wrapper) > 0;
    }

    @Override
    public <D> List<D> queryList(Query query, Class<D> dtoClazz) {
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapperBuilder<>(query, clazz, dtoClazz).build();
        return baseMapper.selectJoinList(dtoClazz, wrapper);
    }

    @Override
    public <D> Pager<D> queryPage(PagerQuery query, Class<D> dtoClazz) {
        Pager<D> pager = new Pager<>(query.getCurrent(), query.getSize());
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapperBuilder(query, clazz, dtoClazz).build();
        return baseMapper.selectJoinPage(pager, dtoClazz, wrapper);
    }

    @Override
    public <D> D getOne(Query query, Class<D> dtoClazz) {
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapperBuilder<>(query, clazz, dtoClazz).build();
        return baseMapper.selectJoinOne(dtoClazz, wrapper);
    }

    @Override
    public <D> int updateById(UpdateModelWrapper<D> dtoWrapper, Class<D> dtoClazz) {
        Assert.isTrue(EntityUtil.getPkVal(dtoWrapper.getModel(), clazz) != null,
                "[%s]主键不能为空", EntityUtil.getPkName(clazz));
        Query query = new Query(EntityUtil.getPkName(clazz), EntityUtil.getPkVal(dtoWrapper.getModel(), clazz));
        UpdateJoinWrapper<T> wrapper = new UpdateJoinWrapperBuilder<>(query, clazz, dtoClazz).build(dtoWrapper.getModel());
        if (dtoWrapper.get_updateNull()) {
            return baseMapper.updateJoinAndNull(null, wrapper);
        } else {
            return baseMapper.updateJoin(null, wrapper);
        }
    }

    @Override
    public <D> boolean exists(List<Cond> conditions, Class<D> dtoClazz) {
        Query query = new Query();
        query.setConds(conditions);
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapperBuilder<T>(query, clazz, dtoClazz)
                .select(w -> w.select("t." + EntityUtil.getPkName(clazz))) // 主表别名就是t
                .build();
        return baseMapper.selectJoinCount(wrapper) > 0;
    }

    @Override
    public String upload(String row, String col, MultipartFile file) throws IOException {
        return fileManager.getFileService().upload(file);
    }

    @Override
    public File download(String path) {
        return fileManager.getFileService().getFile(path);
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