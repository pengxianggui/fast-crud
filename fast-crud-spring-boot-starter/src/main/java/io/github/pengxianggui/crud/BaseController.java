package io.github.pengxianggui.crud;

import io.github.pengxianggui.crud.query.Pager;
import io.github.pengxianggui.crud.query.PagerQuery;
import io.github.pengxianggui.crud.query.PagerView;
import io.github.pengxianggui.crud.query.Query;
import io.github.pengxianggui.crud.valid.CrudInsert;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Validator;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

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
        return model;
    }

    @ApiOperation("批量插入")
    @PostMapping("insert/batch")
    public List<T> insertBatch(@RequestBody @Validated(CrudInsert.class) List<T> models) {
        baseService.saveBatch(models);
        return models;
    }

    @ApiOperation("更新")
    @PostMapping("update")
    public T update(@RequestBody UpdateModelWrapper<T> modelWrapper) throws BindException {
        modelWrapper.validate(validator);
        baseService.updateById(modelWrapper);
        return baseService.getById(modelWrapper.getPkVal());
    }

    @ApiOperation("列表查询")
    @PostMapping("list")
    public List<T> query(@RequestBody @Validated Query query) {
        return baseService.list(query.wrapper());
    }

    @ApiOperation("分页查询")
    @PostMapping("page")
    public PagerView<T> page(@RequestBody @Validated PagerQuery query) {
        Pager<T> pager = query.toPager();
        pager = baseService.page(pager, pager.wrapper());
        return pager.toView();
    }

    @ApiOperation("详情查询")
    @GetMapping("detail/{id}")
    public T detail(@PathVariable Serializable id) {
        return baseService.getById(id);
    }

    @ApiOperation("删除")
    @DeleteMapping("delete/{id}")
    public Boolean delete(@PathVariable Serializable id) {
        return baseService.removeById(id);
    }

    @ApiOperation("批量删除")
    @PostMapping("delete/batch")
    public Boolean deleteBatch(@RequestBody @Validated Serializable... ids) {
        return baseService.removeByIds(Arrays.asList(ids));
    }
}
