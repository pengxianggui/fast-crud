package io.github.pengxianggui.crud;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.pengxianggui.crud.importer.ExcelImportManager;
import io.github.pengxianggui.crud.importer.ImportException;
import io.github.pengxianggui.crud.importer.ImportParseResult;
import io.github.pengxianggui.crud.importer.ImportResult;
import io.github.pengxianggui.crud.query.ImportParam;
import io.github.pengxianggui.crud.util.EntityUtil;
import io.github.pengxianggui.crud.util.ValidUtil;
import io.github.pengxianggui.crud.valid.CrudInsert;
import io.github.pengxianggui.crud.valid.CrudUpdate;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 全量 CRUD Controller 基类。继承此父类的 Controller 可直接获得 fast-table 所需的全部读写接口
 * <p>
 * 推荐用法：
 * <ul>
 *     <li>业务表只读：继承 {@link BaseReadController}；</li>
 *     <li>业务表可写：继承本类；</li>
 *     <li>需要排除个别接口：结合 {@link CrudExclude} 使用。</li>
 * </ul>
 * 注意: 继承父类不是必须的，完全可以基于 service 自行提供 fast-table 所必须的接口。
 *
 * @param <M>
 * @author pengxg
 */
@Slf4j
public class BaseController<M> extends BaseReadController<M> {

    public BaseController(BaseService baseService, Class<M> dtoClazz) {
        super(baseService, dtoClazz);
    }

    /**
     * [FC] 新增一条记录
     *
     * @param model
     * @return 新增成功的条数
     */
    @ApiOperation("插入")
    @PostMapping("insert")
    public int insert(@RequestBody @Validated(CrudInsert.class) M model) {
        return dtoClazz.equals(entityClazz)
                ? baseService.insert(model)
                : baseService.insert(model, dtoClazz);
    }

    /**
     * [FC] (批量)新增多条记录
     *
     * @param models
     * @return 新增成功的条数
     * @throws BindException
     */
    @ApiOperation("批量插入")
    @PostMapping("insert/batch")
    public int insertBatch(@RequestBody List<M> models) throws BindException {
        if (CollectionUtil.isEmpty(models)) return 0;
        for (M model : models) {
            ValidUtil.valid(validator, model, CrudInsert.class);
        }
        return dtoClazz.equals(entityClazz)
                ? baseService.insertBatch(models)
                : baseService.insertBatch(models, dtoClazz);
    }

    /**
     * [FC] 修改一条记录
     *
     * @param modelWrapper
     * @return 修改成功的条数
     * @throws BindException
     */
    @ApiOperation("编辑")
    @PostMapping("update")
    public int update(@RequestBody @Validated(CrudUpdate.class) UpdateModelWrapper<M> modelWrapper) throws BindException {
        return dtoClazz.equals(entityClazz)
                ? baseService.updateById(modelWrapper.getModel()) ? 1 : 0
                : baseService.update(modelWrapper.getModel(), dtoClazz, ObjectUtil.defaultIfNull(modelWrapper.get_updateNull(), true));
    }

    /**
     * [FC] (批量)修改多条记录
     *
     * @param models
     * @return 修改成功的条数
     * @throws BindException
     */
    @ApiOperation(value = "批量编辑", notes = "不支持个性化选择_updateNull")
    @PostMapping("update/batch")
    public int updateBatch(@RequestBody List<M> models) throws BindException {
        if (CollectionUtil.isEmpty(models)) return 0;
        for (M model : models) {
            ValidUtil.valid(validator, model, CrudUpdate.class);
        }
        return dtoClazz.equals(entityClazz)
                ? baseService.updateBatchById(models) ? 1 : 0
                : baseService.updateBatch(models, dtoClazz, true);
    }

    /**
     * [FC] 删除单条记录
     *
     * @param model
     * @return 返回删除成功的条数
     */
    @ApiOperation("删除")
    @PostMapping("delete")
    public int delete(@RequestBody @Validated @NotNull M model) {
        Serializable id = EntityUtil.getPkVal(model, this.entityClazz);
        Assert.notNull(id, "Can't get primary key value!");
        return baseService.deleteById(id) ? 1 : 0;
    }

    /**
     * [FC] (批量)删除多条记录
     *
     * @param models
     * @return 返回删除成功的条数
     */
    @ApiOperation("批量删除")
    @PostMapping("delete/batch")
    public int deleteBatch(@RequestBody @Validated @NotEmpty List<M> models) {
        Set<Serializable> ids = new HashSet<>(models.size());
        for (int i = 0; i < models.size(); i++) {
            M model = models.get(i);
            Serializable id = EntityUtil.getPkVal(model, this.entityClazz);
            Assert.notNull(id, "Can't get primary key value at index {}", i);
            ids.add(id);
        }
        return baseService.deleteBatchById(ids) ? ids.size() : 0;
    }

    /**
     * [FC] 上传
     *
     * @param row  上传字段所在行的记录(json字符串)
     * @param col  上传字段名
     * @param file 文件
     * @return 返回文件的下载地址
     * @throws IOException
     */
    @ApiOperation(value = "上传", notes = "某个字段为图片/文件字段时需要使用上传接口")
    @PostMapping("upload")
    public String upload(@ApiParam("上传字段所在行的记录(json字符串)") @RequestParam(value = "row", required = false) String row,
                         @ApiParam("上传字段") @RequestParam(value = "col", required = false) String col,
                         MultipartFile file) throws IOException {
        String filePath = baseService.upload(row, col, file);
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            return filePath;
        }
        RequestMapping requestMapping = this.getClass().getAnnotation(RequestMapping.class);
        String basePath = (requestMapping != null ? requestMapping.value()[0] : "");
        return String.format("%s/download?path=%s", StrUtil.addPrefixIfNot(basePath, "/"), URLEncoder.encode(filePath));
    }

    /**
     * [FC] (批量)导入
     *
     * @param param   导入参数(列配置、额外参数), 对标{@link io.github.pengxianggui.crud.query.ExportParam}
     * @param file    excel文件(xlsx/xls)
     * @return 导入结果
     * @apiNote 请求为multipart/form-data: part[param]为json(见{@link ImportParam}), part[file]为excel文件。导入的excel模板通过导出接口下载(导出参数template = true)。导入时会自动区分新增与更新:行内主键有值则更新; 无主键时使用列配置中importUnique=true的列组合匹配已有记录, 命中则更新, 否则新增。整个过程在同一个事务中执行, 任一行校验/写入失败则整批回滚, 并以success=false返回行级错误明细。
     */
    @ApiOperation(value = "导入", notes = "导入excel数据, 自动区分新增/更新, 失败整批回滚")
    @PostMapping("import")
    public ImportResult importData(@ApiParam("导入参数(json)") @RequestPart("param") @Validated ImportParam param,
                                   @ApiParam("excel文件(xlsx/xls)") @RequestPart("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ImportResult.fail("Choose file first please!");
            }
            List<Map<String, Object>> columnList = param.getColumns();
            Map<String, Object> extraMap = param.getExtra() == null ? new HashMap<>() : param.getExtra();
            ExcelImportManager excelImportManager = new ExcelImportManager(objectMapper);
            ImportParseResult<M> parseResult = excelImportManager.parse(file.getInputStream(), columnList, dtoClazz);
            if (CollectionUtil.isNotEmpty(parseResult.getErrors())) {
                return ImportResult.fail(parseResult.getRows().size() + parseResult.getErrors().size(),
                        parseResult.getErrors());
            }
            if (CollectionUtil.isEmpty(parseResult.getRows())) {
                return ImportResult.fail("No importable data found in the excel (please make sure the header matches the template)");
            }
            return baseService.importData(parseResult.getRows(), dtoClazz, columnList, extraMap);
        } catch (ImportException e) {
            return ImportResult.fail(0, e.getErrors());
        } catch (Exception e) {
            log.error("Fast crud import error", e);
            return ImportResult.fail(StrUtil.blankToDefault(e.getMessage(), "Import failed"));
        }
    }
}
