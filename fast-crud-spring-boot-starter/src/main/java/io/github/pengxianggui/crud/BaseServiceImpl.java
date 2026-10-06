package io.github.pengxianggui.crud;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.base.MPJBaseMapper;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.DeleteJoinWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.github.yulichang.wrapper.UpdateJoinWrapper;
import io.github.pengxianggui.crud.file.FileManager;
import io.github.pengxianggui.crud.importer.ImportContext;
import io.github.pengxianggui.crud.importer.ImportError;
import io.github.pengxianggui.crud.importer.ImportException;
import io.github.pengxianggui.crud.importer.ImportResult;
import io.github.pengxianggui.crud.importer.ImportRow;
import io.github.pengxianggui.crud.join.*;
import io.github.pengxianggui.crud.join.MethodReferenceRegistry;
import io.github.pengxianggui.crud.query.Cond;
import io.github.pengxianggui.crud.query.PagerQuery;
import io.github.pengxianggui.crud.query.Query;
import io.github.pengxianggui.crud.query.QueryWrapperUtil;
import io.github.pengxianggui.crud.util.EntityUtil;
import io.github.pengxianggui.crud.util.ValidUtil;
import io.github.pengxianggui.crud.valid.CrudInsert;
import io.github.pengxianggui.crud.valid.CrudUpdate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public abstract class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements BaseService<T> {
    // 以下三个通过属性注入，为避免spring代理影响，当前类中使用必须用get方法(参考ServiceImpl中对baseMapper的使用)
    @Getter
    @Autowired
    protected FileManager fileManager;
    @Getter
    @Autowired
    protected CommonRepo commonRepo;
    @Getter
    @Autowired(required = false)
    private PlatformTransactionManager transactionManager;
    /**
     * 用于导入时的数据校验(复用与insert/update相同的校验注解与分组)
     */
    @Autowired(required = false)
    protected Validator validator;

    /**
     * 实体类中主键字段名
     */
    @Getter
    protected String pkName;
    /**
     * 数据表中主键字段名
     */
    @Getter
    protected String dbPkName;

    @PostConstruct
    public void init() {
        Class<T> clazz = this.getEntityClass();
        this.pkName = EntityUtil.getPkName(clazz);
        this.dbPkName = EntityUtil.getDbPkName(clazz);
    }

    /**
     * 插入前的钩子。通常用于自定义数据校验，以及生成主键值(UUID/雪花ID)、某些编码字段值、关联冗余字段值
     *
     * @param entity
     * @return 若返回false则不触发后续插入
     */
    protected boolean beforeInsert(T entity) {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public int insert(T entity) {
        if (this.beforeInsert(entity) == Boolean.FALSE) {
            return 0;
        }
        boolean flag = this.save(entity);
        if (flag) {
            this.afterInsert(entity);
        }
        return flag ? 1 : 0;
    }

    /**
     * 插入成功后的钩子。通常用于触发一些级联操作
     *
     * @param entity
     */
    protected void afterInsert(T entity) {
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public int insertBatch(List<T> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return 0;
        }
        List<T> insertEntities = entities.stream().filter(this::beforeInsert).collect(Collectors.toList());
        this.saveBatch(insertEntities); // saveBatch返回的true严格意义上讲不代表全部执行成功，这里视为全成功也是无奈之举
        insertEntities.forEach(entity -> this.afterInsert(entity));
        return insertEntities.size();
    }

    /**
     * 列表查询前的钩子。注意: query已被解析进wrapper，除了扩展字段{@link Query#getExtra()}, 通常你继承此方法实现扩展字段的查询逻辑
     *
     * @param query
     * @param wrapper
     */
    protected void beforeQueryList(Query query, QueryWrapper<T> wrapper) {
    }

    @Override
    public List<T> queryList(Query query) {
        Assert.notNull(query, "query can not be null!");
        QueryWrapper<T> wrapper = QueryWrapperUtil.build(query, getEntityClass());
        this.beforeQueryList(query, wrapper);
        return list(wrapper);
    }

    @Override
    public T queryOne(Query query) {
        QueryWrapper<T> queryWrapper = QueryWrapperUtil.build(query, getEntityClass());
        queryWrapper.last(" LIMIT 1");
        List<T> list = list(queryWrapper);
        return CollectionUtil.isNotEmpty(list) ? list.get(0) : null;
    }

    /**
     * 分页列表查询前的钩子。注意: query已被解析进wrapper，除了扩展字段{@link Query#getExtra()}, 通常你继承此方法实现扩展字段的查询逻辑
     *
     * @param query
     * @param wrapper
     */
    protected void beforeQueryPage(PagerQuery query, QueryWrapper<T> wrapper) {
    }

    @Override
    public IPage<T> queryPage(PagerQuery query) {
        Assert.notNull(query, "query can not be null!");
        Page<T> pager = new Page<>(query.getCurrent(), query.getSize());
        QueryWrapper<T> wrapper = QueryWrapperUtil.build(query, getEntityClass());
        this.beforeQueryPage(query, wrapper);
        return page(pager, wrapper);
    }

    /**
     * 更新前的钩子。通常用于自定义数据校验，以及设置某些值
     *
     * @param entity
     * @return 若返回false则不触发后续更新
     */
    protected boolean beforeUpdateById(T entity) {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean updateById(T entity) {
        Assert.notNull(entity, "entity can not be null!");
        if (this.beforeUpdateById(entity) == Boolean.FALSE) {
            return false;
        }
        boolean flag = super.updateById(entity);
        this.afterUpdateById(entity);
        return flag;
    }

    /**
     * 更新成功后的钩子。通常用于触发一些级联操作
     *
     * @param entity
     */
    protected void afterUpdateById(T entity) {
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean updateBatchById(Collection<T> entities) {
        if (CollectionUtil.isEmpty(entities)) {
            return false;
        }
        List<T> updateEntities = entities.stream().filter(this::beforeUpdateById).collect(Collectors.toList());
        boolean flag = super.updateBatchById(updateEntities);
        updateEntities.forEach(this::afterUpdateById);
        return flag;
    }

    public boolean exists(List<Cond> conditions) {
        Class<T> clazz = getEntityClass();
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        QueryWrapperUtil.addConditions(wrapper, conditions, clazz);
        return this.count(wrapper) > 0;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public boolean deleteById(Serializable id) {
        if (beforeDeleteById(id) == Boolean.FALSE) {
            return false;
        }
        boolean flag = removeById(id);
        if (flag) {
            afterDeleteById(id);
        }
        return flag;
    }

    /**
     * 删除前的钩子。通常用于自定义数据校验。若返回false则不会触发后续流程
     *
     * @param id
     * @return
     */
    protected boolean beforeDeleteById(Serializable id) {
        return true;
    }

    @Override
    public boolean deleteBatchById(Collection<? extends Serializable> ids) {
        if (ids.stream().anyMatch(id -> beforeDeleteById(id) == Boolean.FALSE)) {
            return false;
        }
        boolean flag = removeByIds(ids);
        if (flag) {
            ids.forEach(this::afterDeleteById);
        }
        return flag;
    }

    /**
     * 删除成功后的钩子。通常用于触发一些级联操作
     *
     * @param id
     */
    protected void afterDeleteById(Serializable id) {
    }

    @Override
    public <DTO> List<DTO> queryList(Query query, Class<DTO> dtoClazz) {
        Assert.notNull(query, "query can not be null!");
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapperBuilder<T>(dtoClazz)
                .query(query)
                .build();
        BaseMapper<T> baseMapper = getBaseMapper();
        if (!(baseMapper instanceof MPJBaseMapper)) {
            throw new ClassCastException("baseMapper is not MPJBaseMapper, please extends MPJBaseMapper");
        }
        return ((MPJBaseMapper<T>) baseMapper).selectJoinList(dtoClazz, wrapper);
    }

    @Override
    public <DTO> DTO queryOne(Query query, Class<DTO> dtoClazz) {
        List<DTO> list = queryList(query, dtoClazz);
        return CollectionUtil.isNotEmpty(list) ? list.get(0) : null;
    }

    @Override
    public <DTO> IPage<DTO> queryPage(PagerQuery query, Class<DTO> dtoClazz) {
        Assert.notNull(query, "query can not be null!");
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        Page<DTO> pager = new Page<>(query.getCurrent(), query.getSize());
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapperBuilder<T>(dtoClazz)
                .query(query)
                .build();

        BaseMapper<T> baseMapper = getBaseMapper();
        if (!(baseMapper instanceof MPJBaseMapper)) {
            throw new ClassCastException("baseMapper is not MPJBaseMapper, please extends MPJBaseMapper");
        }
        return ((MPJBaseMapper<T>) baseMapper).selectJoinPage(pager, dtoClazz, wrapper);
    }

    @Override
    public <DTO> DTO getById(Serializable id, Class<DTO> dtoClazz) {
        Assert.notNull(id, "id can not be null!");
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapperBuilder<T>(dtoClazz)
                .where(w -> w.eq("t." + getDbPkName(), id))
                .build();

        BaseMapper<T> baseMapper = getBaseMapper();
        if (!(baseMapper instanceof MPJBaseMapper)) {
            throw new ClassCastException("baseMapper is not MPJBaseMapper, please extends MPJBaseMapper");
        }
        return ((MPJBaseMapper<T>) baseMapper).selectJoinOne(dtoClazz, wrapper);
    }

    @Override
    public <DTO> int insert(DTO model, Class<DTO> dtoClazz) {
        Assert.notNull(model, "model can not be null!");
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        AtomicInteger count = new AtomicInteger();
        T mainEntity = (T) EntityReverseParser.createMainInstanceForInset(model);
        executeInTransaction(() -> {
            count.addAndGet(insert(mainEntity));
            List<Object> joinEntities = EntityReverseParser.createJoinInstanceForInsert(model, mainEntity);
            for (Object joinEntity : joinEntities) {
                count.addAndGet(getCommonRepo().saveOrUpdate(joinEntity.getClass(), joinEntity));
            }
        });
        return count.get();
    }

    @Override
    public <DTO> int insertBatch(Collection<DTO> modelList, Class<DTO> dtoClazz) {
        if (CollectionUtil.isEmpty(modelList)) {
            return 0;
        }
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        AtomicInteger count = new AtomicInteger();
        executeInTransaction(() -> {
            count.addAndGet(modelList.stream().mapToInt(m -> this.insert(m, dtoClazz)).sum());
        });
        return count.get();
    }

    @Override
    public <DTO> int update(DTO model, Class<DTO> dtoClazz, boolean updateNull) {
        Assert.notNull(model, "model can not be null!");
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        Class<T> clazz = getEntityClass();
        String pkName = getPkName();
        Serializable pkValue = EntityUtil.getPkVal(model, clazz);
        Assert.isTrue(pkValue != null, "Primary key can't be null![{}={}]", pkName, pkValue);
        // TODO 使用update..set.. 的方式会导致一些问题, 比如: update_time和update_by等审计字段不好兼容。尝试换成构造entity，使用update entity更新
        UpdateJoinWrapper<T> wrapper = new UpdateJoinWrapperBuilder<T, DTO>(dtoClazz)
                .set(model)
                .where(w -> w.eq(MethodReferenceRegistry.getFunction(clazz, pkName), pkValue))
                .updateNull(updateNull)
                .build();

        BaseMapper<T> baseMapper = getBaseMapper();
        if (!(baseMapper instanceof MPJBaseMapper)) {
            throw new ClassCastException("baseMapper is not MPJBaseMapper, please extends MPJBaseMapper");
        }
        return ((MPJBaseMapper<T>) baseMapper).updateJoin(null, wrapper);
    }

    @Override
    public <DTO> int updateBatch(List<DTO> models, Class<DTO> dtoClazz, boolean updateNull) {
        if (CollectionUtil.isEmpty(models)) {
            return 0;
        }
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        AtomicInteger count = new AtomicInteger();
        executeInTransaction(() -> {
            for (DTO model : models) {
                count.addAndGet(update(model, dtoClazz, updateNull));
            }
        });
        return count.get();
    }

    /**
     * 导入前的钩子。可用于回填额外参数(如customerId)、补充默认值, 或做自定义业务校验。
     * 抛出异常将终止导入并整批回滚。
     *
     * @param context 导入上下文, 其中rows可被直接修改
     */
    protected <DTO> void beforeImport(ImportContext<DTO> context) {
    }

    /**
     * 导入时(走DTO更新路径)是否把DTO中的null字段也更新到数据库。
     * <p>
     * 默认false: 只更新excel中真正导入的列, 避免把未导入的列(如create_time等审计字段, 或importable=false的列)置空。
     * 若业务确实希望通过导入把某些列清空, 可覆写本方法返回true(注意: 此时未导入的列都会被置空)
     *
     * @return true-null字段也更新; false-只更新非null字段
     */
    protected boolean importUpdateNull() {
        return false;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public <DTO> ImportResult importData(List<ImportRow<DTO>> rows,
                                         Class<DTO> dtoClazz,
                                         List<Map<String, Object>> columns,
                                         Map<String, Object> extra) {
        if (CollectionUtil.isEmpty(rows)) {
            return ImportResult.success(0, 0, 0);
        }
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        ImportContext<DTO> context = new ImportContext<>(rows, dtoClazz, columns, extra);
        // 1. 业务扩展钩子(回填额外参数、自定义校验等)
        this.beforeImport(context);

        List<ImportError> errors = new ArrayList<>();
        // 2. 解析无主键行的匹配主键(基于importUnique列组合匹配)
        Map<Integer, Serializable> matchedPkMapping = resolveImportMatchedPk(rows, context.getImportUniqueCols(), errors);
        if (!errors.isEmpty()) {
            throw new ImportException(errors);
        }

        // 3. 整批校验(复用insert/update相同的校验注解与分组), 校验不通过则不落库
        Map<String, String> labelMapping = buildColLabelMapping(columns);
        for (int i = 0; i < rows.size(); i++) {
            ImportRow<DTO> row = rows.get(i);
            boolean update = getDtoPkVal(row.getModel()) != null || matchedPkMapping.containsKey(i);
            if (validator == null) {
                continue;
            }
            try {
                ValidUtil.valid(validator, row.getModel(), update ? CrudUpdate.class : CrudInsert.class);
            } catch (BindException e) {
                errors.add(new ImportError(row.getRow(), null, null, joinFieldErrors(e, labelMapping)));
            } catch (Exception e) {
                errors.add(new ImportError(row.getRow(), null, null, rootMessage(e)));
            }
        }
        if (!errors.isEmpty()) {
            throw new ImportException(errors);
        }

        // 4. 逐行写入, 复用insert/update及其钩子(insert: beforeInsert/afterInsert; update: beforeUpdateById/afterUpdateById)
        int inserted = 0;
        int updated = 0;
        List<ImportError> writeErrors = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            ImportRow<DTO> row = rows.get(i);
            DTO model = row.getModel();
            Serializable pk = getDtoPkVal(model);
            if (pk == null && matchedPkMapping.containsKey(i)) {
                pk = matchedPkMapping.get(i);
                setDtoPkVal(model, pkName, pk);
            }
            try {
                if (pk == null) {
                    int count = dtoClazz.equals(getEntityClass()) ? this.insert((T) model) : this.insert(model, dtoClazz);
                    if (count > 0) {
                        inserted++;
                    } else {
                        writeErrors.add(new ImportError(row.getRow(), null, null,
                                "Insert not applied (maybe blocked by the beforeInsert hook)"));
                    }
                } else {
                    if (dtoClazz.equals(getEntityClass())) {
                        if (this.updateById((T) model)) {
                            updated++;
                        } else {
                            writeErrors.add(new ImportError(row.getRow(), null, null,
                                    "Update not applied (maybe blocked by the beforeUpdateById hook)"));
                        }
                    } else {
                        // 只更新excel中导入的列, 避免把未导入的列(如create_time等审计字段)置空
                        this.update(model, dtoClazz, importUpdateNull());
                        updated++;
                    }
                }
            } catch (Exception e) {
                writeErrors.add(new ImportError(row.getRow(), null, null, rootMessage(e)));
            }
        }
        if (!writeErrors.isEmpty()) {
            throw new ImportException(writeErrors);
        }
        return ImportResult.success(rows.size(), inserted, updated);
    }

    /**
     * 基于importUnique列, 为无主键的导入行匹配已有记录的主键值。
     *
     * @return key为行下标(rows的index), value为匹配到的主键值
     */
    private <DTO> Map<Integer, Serializable> resolveImportMatchedPk(List<ImportRow<DTO>> rows,
                                                                    List<String> uniqueCols, List<ImportError> errors) {
        Map<Integer, Serializable> result = new HashMap<>();
        if (CollectionUtil.isEmpty(uniqueCols)) {
            return result;
        }
        Class<T> entityClazz = getEntityClass();
        Map<String, String> dbColMapping = new LinkedHashMap<>();
        for (String col : uniqueCols) {
            String dbCol = EntityUtil.getDbFieldName(entityClazz, col);
            if (StrUtil.isBlank(dbCol)) {
                errors.add(ImportError.of(StrUtil.format(
                        "Column [{}] is not a field of the main table, cannot be used as an importUnique match key", col)));
            } else {
                dbColMapping.put(col, dbCol);
            }
        }
        if (!errors.isEmpty()) {
            return result;
        }

        // 收集需要按唯一键匹配的行(仅无主键的行)
        Map<String, List<Integer>> keyRowMapping = new LinkedHashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            DTO model = rows.get(i).getModel();
            if (getDtoPkVal(model) != null) {
                continue;
            }
            List<Object> values = new ArrayList<>(uniqueCols.size());
            boolean anyNull = false;
            for (String col : uniqueCols) {
                Object value = ReflectUtil.getFieldValue(model, col);
                if (value == null) {
                    anyNull = true;
                    break;
                }
                values.add(value);
            }
            if (anyNull) { // 匹配键不完整, 视为新增
                continue;
            }
            keyRowMapping.computeIfAbsent(buildMatchKey(values), k -> new ArrayList<>()).add(i);
        }
        if (keyRowMapping.isEmpty()) {
            return result;
        }

        // 一次批量查询, 避免逐行查询
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        for (String col : uniqueCols) {
            Set<Object> values = rows.stream()
                    .map(row -> ReflectUtil.getFieldValue(row.getModel(), col))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (CollectionUtil.isEmpty(values)) {
                return result;
            }
            wrapper.in(dbColMapping.get(col), values);
        }
        List<T> exists = this.list(wrapper);

        Map<String, List<Serializable>> existingMapping = new HashMap<>();
        for (T entity : exists) {
            List<Object> values = uniqueCols.stream()
                    .map(col -> ReflectUtil.getFieldValue(entity, col))
                    .collect(Collectors.toList());
            if (values.stream().anyMatch(Objects::isNull)) {
                continue;
            }
            existingMapping.computeIfAbsent(buildMatchKey(values), k -> new ArrayList<>())
                    .add(EntityUtil.getPkVal(entity));
        }

        for (Map.Entry<String, List<Integer>> entry : keyRowMapping.entrySet()) {
            List<Serializable> pks = existingMapping.get(entry.getKey());
            if (CollectionUtil.isEmpty(pks)) { // 未命中, 视为新增
                continue;
            }
            if (pks.size() > 1) {
                for (Integer rowIndex : entry.getValue()) {
                    errors.add(new ImportError(rows.get(rowIndex).getRow(), null, null,
                            StrUtil.format("Multiple records matched by [{}], unable to determine the update target",
                                    String.join(",", uniqueCols))));
                }
                continue;
            }
            for (Integer rowIndex : entry.getValue()) {
                result.put(rowIndex, pks.get(0));
            }
        }
        return result;
    }

    /**
     * 获取导入行对象上的主键值(entity主键字段名在DTO中的同名字段)
     */
    private Serializable getDtoPkVal(Object model) {
        if (model == null) {
            return null;
        }
        return EntityUtil.getPkVal(model, getEntityClass());
    }

    private void setDtoPkVal(Object model, String pkName, Serializable pk) {
        ReflectUtil.setFieldValue(model, pkName, pk);
    }

    private static String buildMatchKey(List<Object> values) {
        return values.stream()
                .map(value -> value == null ? "" : String.valueOf(value).trim())
                .collect(Collectors.joining("\u0001"));
    }

    private static Map<String, String> buildColLabelMapping(List<Map<String, Object>> columns) {
        Map<String, String> mapping = new HashMap<>();
        if (CollectionUtil.isEmpty(columns)) {
            return mapping;
        }
        for (Map<String, Object> column : columns) {
            String col = (String) column.get("col");
            String label = (String) column.get("label");
            if (StrUtil.isNotBlank(col) && StrUtil.isNotBlank(label)) {
                mapping.putIfAbsent(col, label);
            }
        }
        return mapping;
    }

    private static String joinFieldErrors(BindException e, Map<String, String> labelMapping) {
        return e.getFieldErrors().stream()
                .map(fieldError -> {
                    String label = labelMapping.getOrDefault(fieldError.getField(), fieldError.getField());
                    return StrUtil.format("[{}]{}", label, fieldError.getDefaultMessage());
                })
                .collect(Collectors.joining("; "));
    }

    private static String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return StrUtil.blankToDefault(root.getMessage(), root.getClass().getSimpleName());
    }

    @Deprecated
    @Override
    public <DTO> int removeById(Serializable id, Class<DTO> dtoClazz) {
        Assert.notNull(id, "id can not be null!");
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        Class<T> clazz = getEntityClass();
        DeleteJoinWrapper<T> wrapper = JoinWrappers.delete(clazz)
                .deleteAll()
                .eq("t." + getDbPkName(), id);
        JoinWrapperUtil.addJoin(wrapper, dtoClazz);

        BaseMapper<T> baseMapper = getBaseMapper();
        if (!(baseMapper instanceof MPJBaseMapper)) {
            throw new ClassCastException("baseMapper is not MPJBaseMapper, please extends MPJBaseMapper");
        }
        return ((MPJBaseMapper<T>) baseMapper).deleteJoin(wrapper);
    }

    @Deprecated
    @Override
    public <DTO> int removeByIds(Collection<? extends Serializable> ids, Class<DTO> dtoClazz) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        Class<T> clazz = getEntityClass();
        DeleteJoinWrapper<T> wrapper = JoinWrappers.delete(clazz)
                .deleteAll()
                .in("t." + getDbPkName(), ids);
        JoinWrapperUtil.addJoin(wrapper, dtoClazz);

        BaseMapper<T> baseMapper = getBaseMapper();
        if (!(baseMapper instanceof MPJBaseMapper)) {
            throw new ClassCastException("baseMapper is not MPJBaseMapper, please extends MPJBaseMapper");
        }
        return ((MPJBaseMapper<T>) baseMapper).deleteJoin(wrapper);
    }

    @Override
    public <DTO> boolean exists(List<Cond> conditions, Class<DTO> dtoClazz) {
        Assert.notNull(dtoClazz, "dtoClazz can not be null!");
        Class<T> clazz = getEntityClass();
        Query query = new Query();
        query.setConds(conditions);
        MPJLambdaWrapper<T> wrapper = new MPJLambdaWrapper<>(clazz);
        JoinWrapperUtil.addJoin(wrapper, dtoClazz);
        JoinWrapperUtil.addConditions(wrapper, conditions, dtoClazz);

        BaseMapper<T> baseMapper = getBaseMapper();
        if (!(baseMapper instanceof MPJBaseMapper)) {
            throw new ClassCastException("baseMapper is not MPJBaseMapper, please extends MPJBaseMapper");
        }
        return ((MPJBaseMapper<T>) baseMapper).selectJoinCount(wrapper) > 0;
    }

    @Override
    public String upload(String row, String col, MultipartFile file) throws IOException {
        return getFileManager().getFileService().upload(file, DateUtils.format(new Date(), "yyyyMMdd"));
    }

    @Override
    public File download(String path) {
        return getFileManager().getFileService().getFile(path);
    }

    private void executeInTransaction(Runnable runnable) {
        PlatformTransactionManager transactionManager = getTransactionManager();
        boolean enableTransaction = transactionManager != null;
        if (enableTransaction == Boolean.FALSE) {
            runnable.run();
            return;
        }

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            runnable.run();
            transactionManager.commit(status);
        } catch (Throwable e) {
            transactionManager.rollback(status);
            throw e;
        }
    }

}
