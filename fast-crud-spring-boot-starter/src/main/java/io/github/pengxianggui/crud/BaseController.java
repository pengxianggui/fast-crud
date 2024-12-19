package io.github.pengxianggui.crud;

import cn.hutool.core.lang.Assert;
import io.github.pengxianggui.crud.meta.EntityUtil;
import io.github.pengxianggui.crud.query.*;
import io.github.pengxianggui.crud.valid.CrudInsert;
import io.github.pengxianggui.crud.valid.CrudUpdate;
import io.github.pengxianggui.crud.valid.ValidUtil;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;
import io.swagger.annotations.ApiOperation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class BaseController<T> {
    private BaseService<T> baseService;
    public Validator validator;

    public BaseController(BaseService<T> baseService, Validator validator) {
        this.baseService = baseService;
        this.validator = validator;
    }

    @ApiOperation("插入")
    @PostMapping("insert")
    public T insert(@RequestBody @Validated(CrudInsert.class) T model) {
        baseService.save(model);
        return baseService.getById(EntityUtil.getPkVal(model));
    }

    @ApiOperation("批量插入")
    @PostMapping("insert/batch")
    public List<T> insertBatch(@RequestBody @Validated(CrudInsert.class) List<T> models) {
        baseService.saveBatch(models);
        return baseService.listByIds(models.stream().map(EntityUtil::getPkVal).collect(Collectors.toList()));
    }

    @ApiOperation("更新")
    @PostMapping("update")
    public T update(@RequestBody UpdateModelWrapper<T> modelWrapper) throws BindException {
        ValidUtil.valid(validator, modelWrapper, CrudUpdate.class);
        baseService.updateById(modelWrapper);
        return baseService.getById(EntityUtil.getPkVal(modelWrapper.getModel()));
    }

    @ApiOperation(value = "批量更新", notes = "不支持个性化选择_updateNull")
    @PostMapping("update/batch")
    @Transactional(rollbackFor = Exception.class)
    public List<T> updateBatch(@RequestBody List<T> models) throws BindException {
        for (T model : models) {
            ValidUtil.valid(validator, model, CrudUpdate.class);
        }
        List<T> result = new ArrayList<>(models.size());
        for (T model : models) {
            baseService.updateById(model);
            result.add(baseService.getById(EntityUtil.getPkVal(model)));
        }
        return result;
    }

    @ApiOperation("列表查询")
    @PostMapping("list")
    public List<T> list(@RequestBody @Validated Query query) {
        return baseService.queryList(query);
    }

    @ApiOperation("分页查询")
    @PostMapping("page")
    public PagerView<T> page(@RequestBody @Validated PagerQuery query) {
        Pager<T> pager = baseService.queryPage(query);
        return pager.toView();
    }

    @ApiOperation("详情查询")
    @PostMapping("detail")
    public T detail(@NotNull @RequestBody T model) {
        Serializable id = EntityUtil.getPkVal(model);
        Assert.notNull(id, "无法获取主键值");
        return baseService.getById(id);
    }

    @ApiOperation("删除")
    @PostMapping("delete")
    public boolean delete(@NotNull @RequestBody T model) {
        Serializable id = EntityUtil.getPkVal(model);
        Assert.notNull(id, "无法获取主键值");
        return baseService.removeById(id);
    }

    @ApiOperation("批量删除")
    @PostMapping("delete/batch")
    public boolean deleteBatch(@NotBlank @NotNull @RequestBody List<T> models) {
        Set<Serializable> ids = new HashSet<>(models.size());
        for (int i = 0; i < models.size(); i++) {
            T model = models.get(i);
            Serializable id = EntityUtil.getPkVal(model);
            Assert.notNull(id, "第%d条数据无法获取主键值", i + 1);
            ids.add(id);
        }
        return baseService.removeByIds(ids);
    }

    @ApiOperation(value = "存在性查询", notes = "指定条件存在数据")
    @PostMapping("exists")
    public Boolean exists(@RequestBody List<Cond> conditions) {
        return baseService.exists(conditions);
    }
}
