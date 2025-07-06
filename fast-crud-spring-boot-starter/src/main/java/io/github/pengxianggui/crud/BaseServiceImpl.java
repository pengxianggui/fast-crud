package io.github.pengxianggui.crud;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.DeleteJoinWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.github.yulichang.wrapper.UpdateJoinWrapper;
import io.github.pengxianggui.crud.dao.BaseMapper;
import io.github.pengxianggui.crud.file.FileManager;
import io.github.pengxianggui.crud.join.*;
import io.github.pengxianggui.crud.query.Cond;
import io.github.pengxianggui.crud.query.PagerQuery;
import io.github.pengxianggui.crud.query.Query;
import io.github.pengxianggui.crud.query.QueryWrapperUtil;
import io.github.pengxianggui.crud.util.EntityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class BaseServiceImpl<T, M extends BaseMapper<T>> extends ServiceImpl<M, T> implements BaseService<T> {
    @Resource
    protected FileManager fileManager;
    @Resource
    private CommonRepo commonRepo;
    @Resource
    private PlatformTransactionManager transactionManager;

    protected String getPkName() {
        Class<T> clazz = this.getEntityClass();
        String pkName = EntityUtil.getPkName(clazz);
        Assert.isTrue(pkName != null && StrUtil.isNotBlank(pkName),
                "Can't find the primary key of entity: {}", clazz.getName());
        return pkName;
    }

    @Override
    public List<T> queryList(Query query) {
        Assert.notNull(query, "query can not be null!");
        return list(QueryWrapperUtil.build(query, getEntityClass()));
    }

    @Override
    public IPage<T> queryPage(PagerQuery query) {
        Assert.notNull(query, "query can not be null!");
        Page<T> pager = new Page<>(query.getCurrent(), query.getSize());
        Wrapper<T> wrapper = QueryWrapperUtil.build(query, getEntityClass());
        return page(pager, wrapper);
    }

    @Override
    public boolean updateById(T entity, Boolean updateNull) {
        Assert.notNull(entity, "entity can not be null!");
        if (updateNull == null) {
            return updateById(entity);
        }

        String pkName = getPkName();
        Serializable pkValue = EntityUtil.getPkVal(entity);
        Assert.isTrue(pkValue != null, "主键不能为空: {}={}", pkName, pkValue);
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(pkName, pkValue);
        List<Field> fields = Arrays.stream(ReflectUtil.getFields(getEntityClass()))
                .filter(field -> !EntityUtil.isMarkAsNotDbField(field)).collect(Collectors.toList());
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            if (fieldName.equals(pkName)) { // 主键不更
                continue;
            }
            try {
                Object fieldValue = field.get(entity);
                if (EntityUtil.fieldNeedUpdate(field, fieldValue, updateNull)) {
                    updateWrapper.set(EntityUtil.getDbFieldName(entity, fieldName), fieldValue);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return this.update(updateWrapper);
    }

    public boolean exists(List<Cond> conditions) {
        Class<T> clazz = getEntityClass();
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        QueryWrapperUtil.addConditions(wrapper, conditions, clazz);
        return this.count(wrapper) > 0;
    }

    @Override
    final public <DTO> List<DTO> queryList(Query query, Class<DTO> dtoClazz) {
        Assert.notNull(query, "query can not be null!");
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        Class<T> clazz = getEntityClass();
        if (clazz.equals(dtoClazz)) {
            return (List<DTO>) queryList(query);
        }
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapperBuilder<>(query, clazz, dtoClazz).build();
        return getBaseMapper().selectJoinList(dtoClazz, wrapper);
    }

    @Override
    final public <DTO> DTO queryOne(Query query, Class<DTO> dtoClazz) {
        Assert.notNull(query, "query can not be null!");
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        Class<T> clazz = getEntityClass();
        if (clazz.equals(dtoClazz)) {
            QueryWrapper<T> queryWrapper = QueryWrapperUtil.build(query, clazz);
            queryWrapper.last(" LIMIT 1");
            List<T> list = list(queryWrapper);
            return CollectionUtil.isNotEmpty(list) ? (DTO) list.get(0) : null;
        }
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapperBuilder<>(query, clazz, dtoClazz).build();
        wrapper.last(" LIMIT 1");
        List<DTO> list = getBaseMapper().selectJoinList(dtoClazz, wrapper);
        return CollectionUtil.isNotEmpty(list) ? list.get(0) : null;
    }

    @Override
    final public <DTO> IPage<DTO> queryPage(PagerQuery query, Class<DTO> dtoClazz) {
        Assert.notNull(query, "query can not be null!");
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        Class<T> clazz = getEntityClass();
        if (clazz.equals(dtoClazz)) {
            return (IPage<DTO>) queryPage(query);
        }
        Page<DTO> pager = new Page<>(query.getCurrent(), query.getSize());
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapperBuilder(query, clazz, dtoClazz).build();
        return getBaseMapper().selectJoinPage(pager, dtoClazz, wrapper);
    }

    @Override
    final public <DTO> DTO getById(Serializable id, Class<DTO> dtoClazz) {
        Assert.notNull(id, "id can not be null!");
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        Class<T> clazz = getEntityClass();
        if (clazz.equals(dtoClazz)) {
            return (DTO) getById(id);
        }
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapperBuilder<>(new Query(), clazz, dtoClazz).build();
        wrapper.eq("t." + getPkName(), id);
        return getBaseMapper().selectJoinOne(dtoClazz, wrapper);
    }

    @Override
    final public <DTO> int insert(DTO model, Class<DTO> dtoClazz) {
        Assert.notNull(model, "model can not be null!");
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        Class<T> clazz = getEntityClass();
        if (clazz.equals(dtoClazz)) {
            return save((T) model) ? 1 : 0;
        }

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            int count = 0;
            Object mainEntity = EntityReverseParser.createMainInstance(model);
            count += commonRepo.saveOrUpdate(clazz, mainEntity);
            List<Object> joinEntities = EntityReverseParser.createJoinInstance(model, mainEntity);
            for (Object joinEntity : joinEntities) {
                count += commonRepo.saveOrUpdate(joinEntity.getClass(), joinEntity);
            }
            transactionManager.commit(status);
            return count;
        } catch (Throwable e) {
            transactionManager.rollback(status);
            throw new RuntimeException(e);
        }
    }

    @Override
    final public <DTO> int insertBatch(Collection<DTO> modelList, Class<DTO> dtoClazz) {
        if (CollectionUtil.isEmpty(modelList)) {
            return 0;
        }
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        Class<T> clazz = getEntityClass();
        if (clazz.equals(dtoClazz)) {
            return saveBatch((Collection<T>) modelList) ? modelList.size() : 0;
        }
        return modelList.stream().mapToInt(m -> this.insert(m, dtoClazz)).sum();
    }

    @Override
    final public <DTO> int updateById(DTO model, Class<DTO> dtoClazz, Boolean updateNull) {
        Assert.notNull(model, "model can not be null!");
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        Class<T> clazz = getEntityClass();
        if (clazz.equals(dtoClazz)) {
            return updateById((T) model, updateNull) ? 1 : 0;
        }
        String pkName = getPkName();
        Serializable pkValue = EntityUtil.getPkVal(model, clazz);
        Assert.isTrue(pkValue != null, "主键不能为空[{}={}]", pkName, pkValue);
        Query query = new Query("t." + pkName, pkValue);
        UpdateJoinWrapper<T> wrapper = new UpdateJoinWrapperBuilder<>(query, clazz, dtoClazz).build(model, updateNull);
        return getBaseMapper().updateJoin(null, wrapper);
    }

    @Override
    final public <DTO> int updateBatchById(List<DTO> models, Class<DTO> dtoClazz, @Nullable Boolean updateNull) {
        if (CollectionUtil.isEmpty(models)) {
            return 0;
        }
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        Class<T> clazz = getEntityClass();
        if (clazz.equals(dtoClazz)) {
            updateBatchById((Collection<T>) models);
            return models.size();
        }
        int count = 0;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            for (DTO model : models) {
                count += updateById(model, dtoClazz, updateNull);
            }
            transactionManager.commit(status);
        } catch (Throwable e) {
            transactionManager.rollback(status);
        }
        return count;
    }

    @Override
    final public <DTO> int removeById(Serializable id, Class<DTO> dtoClazz) {
        Assert.notNull(id, "id can not be null!");
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        Class<T> clazz = getEntityClass();
        if (clazz.equals(dtoClazz)) {
            return removeById(id) ? 1 : 0;
        }
        DeleteJoinWrapper<T> wrapper = JoinWrappers.delete(clazz)
                .deleteAll()
                .eq("t." + getPkName(), id);
        JoinWrapperUtil.addJoin(wrapper, dtoClazz);
        return getBaseMapper().deleteJoin(wrapper);
    }

    @Override
    final public <DTO> int removeByIds(Collection<? extends Serializable> ids, Class<DTO> dtoClazz) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        Class<T> clazz = getEntityClass();
        if (clazz.equals(dtoClazz)) {
            return removeByIds(ids) ? ids.size() : 0;
        }
        DeleteJoinWrapper<T> wrapper = JoinWrappers.delete(clazz)
                .deleteAll()
                .in("t." + getPkName(), ids);
        JoinWrapperUtil.addJoin(wrapper, dtoClazz);
        return getBaseMapper().deleteJoin(wrapper);
    }

    @Override
    final public <DTO> boolean exists(List<Cond> conditions, Class<DTO> dtoClazz) {
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        Class<T> clazz = getEntityClass();
        if (clazz.equals(dtoClazz)) {
            return exists(conditions);
        }
        Query query = new Query();
        query.setConds(conditions);
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapperBuilder<>(query, clazz, dtoClazz).build();
        return getBaseMapper().selectJoinCount(wrapper) > 0;
    }

    @Override
    public String upload(String row, String col, MultipartFile file) throws IOException {
        return fileManager.getFileService().upload(file);
    }

    @Override
    public File download(String path) {
        return fileManager.getFileService().getFile(path);
    }

}