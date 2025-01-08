package io.github.pengxianggui.crud;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.pengxianggui.crud.meta.EntityUtil;
import io.github.pengxianggui.crud.query.*;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

public abstract class BaseServiceImpl<T, M extends BaseMapper<T>> extends ServiceImpl<M, T> implements BaseService<T> {
    protected Class<T> clazz;

    @PostConstruct
    public abstract void init();

    @Resource
    private FastCrudProperty fastCrudProperty;

    @Override
    public List<T> queryList(Query query) {
        return list(query.wrapper(clazz));
    }

    @Override
    public Pager<T> queryPage(PagerQuery query) {
        Pager<T> pager = query.toPager();
        QueryWrapper<T> queryWrapper = pager.wrapper(clazz);
        return page(pager, queryWrapper);
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
        QueryWrapper<T> wrapper = new QueryWrapper<T>();
        QueryWrapperUtil.addConditions(wrapper, conditions, clazz);
        wrapper.last(" limit 1");
        return this.count(wrapper) > 0;
    }

    @Override
    public String upload(String row, String col, MultipartFile file) throws IOException {
        String uploadDir;
        if (StrUtil.isBlank(fastCrudProperty.getUploadDir())) {
            try {
                Path tempDir = Files.createTempDirectory("upload");
                uploadDir = tempDir.toAbsolutePath().toString();
            } catch (IOException e) {
                throw e;
            }
        } else {
            uploadDir = fastCrudProperty.getUploadDir();
        }
        if (!uploadDir.endsWith(File.separator)) {
            uploadDir += File.separator;
        }
        String fileName = file.getOriginalFilename();
        File targetFile = new File(uploadDir + File.separator + DateUtil.format(new Date(), "yyyyMMddHHmmssSSS") + File.separator + fileName);
        try {
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            file.transferTo(targetFile);
        } catch (IOException e) {
            throw e;
        }
        return buildPreviewFileUrl(targetFile);
    }

    @Override
    public File download(String path) {
        return new File(path);
    }

    /**
     * 构建文件预览地址。在文件资源上传之后会调用此方法。默认将返回文件的绝对路径, 客户端将通过内置的 xxx/download?path=${path}来访问此资源。
     * 若希望上传到oss并返回绝对oss资源路径,可以重写此方法实现oss上传并返回http绝对资源路径，返回给客户端的将是绝对oss资源路径，而不会再通过xxx/download?path=来访问。
     *
     * @param targetFile
     * @return
     */
    protected String buildPreviewFileUrl(File targetFile) {
        return targetFile.getAbsolutePath();
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