package io.github.pengxianggui.crud;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.pengxianggui.crud.download.FileResourceHttpRequestHandler;
import io.github.pengxianggui.crud.export.ExcelExportManager;
import io.github.pengxianggui.crud.query.*;
import io.github.pengxianggui.crud.util.EntityUtil;
import io.github.pengxianggui.crud.util.ValidUtil;
import io.github.pengxianggui.crud.valid.CrudInsert;
import io.github.pengxianggui.crud.valid.CrudUpdate;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class BaseController<M> {
    private final BaseService baseService;
    private final Class<M> dtoClazz;
    private Class<?> entityClazz;
    @Resource
    public Validator validator;

    public BaseController(BaseService baseService, Class<M> dtoClazz) {
        this.baseService = baseService;
        this.dtoClazz = dtoClazz;
        this.entityClazz = baseService.getEntityClass();
    }

    @ApiOperation("插入")
    @PostMapping("insert")
    final public int _insert(@RequestBody @Validated(CrudInsert.class) M model) {
        return this.insert(model);
    }

    @ApiOperation("批量插入")
    @PostMapping("insert/batch")
    final public int _insertBatch(@RequestBody List<M> models) throws BindException {
        if (CollectionUtil.isEmpty(models)) return 0;
        for (M model : models) {
            ValidUtil.valid(validator, model, CrudInsert.class);
        }
        return this.insertBatch(models);
    }

    @ApiOperation("编辑")
    @PostMapping("update")
    final public int _update(@Validated(CrudUpdate.class) @RequestBody UpdateModelWrapper<M> modelWrapper) throws BindException {
        return this.update(modelWrapper.getModel(), modelWrapper.get_updateNull());
    }

    @ApiOperation(value = "批量编辑", notes = "不支持个性化选择_updateNull")
    @PostMapping("update/batch")
    final public int _updateBatch(@RequestBody List<M> models) throws BindException {
        if (CollectionUtil.isEmpty(models)) return 0;
        for (M model : models) {
            ValidUtil.valid(validator, model, CrudUpdate.class);
        }
        return this.updateBatch(models);
    }

    @ApiOperation("列表查询")
    @PostMapping("list")
    final public List<M> _list(@RequestBody @Validated Query query) {
        return this.list(query);
    }

    @ApiOperation("分页查询")
    @PostMapping("page")
    final public PagerView<M> _page(@RequestBody @Validated PagerQuery query) {
        return this.page(query);
    }

    @ApiOperation("详情")
    @GetMapping("{id}/detail")
    final public M _detail(@PathVariable Serializable id) {
        return this.detail(id);
    }

    @ApiOperation("删除")
    @PostMapping("delete")
    final public int _delete(@NotNull @RequestBody M model) {
        return this.delete(model);
    }

    @ApiOperation("批量删除")
    @PostMapping("delete/batch")
    final public int _deleteBatch(@NotBlank @NotNull @RequestBody List<M> models) {
        return deleteBatch(models);
    }

    @ApiOperation(value = "存在性查询", notes = "指定条件存在数据")
    @PostMapping("exists")
    final public Boolean _exists(@RequestBody List<Cond> conditions) {
        return this.exists(conditions);
    }


    protected int insert(M model) {
        return dtoClazz.equals(entityClazz)
                ? baseService.insert(model)
                : baseService.insert(model, dtoClazz);
    }

    protected int insertBatch(List<M> models) {
        return dtoClazz.equals(entityClazz)
                ? baseService.insertBatch(models)
                : baseService.insertBatch(models, dtoClazz);
    }

    protected int update(M model, Boolean updateNull) throws BindException {
        return dtoClazz.equals(entityClazz)
                ? baseService.updateById(model, updateNull)
                : baseService.updateById(model, dtoClazz, ObjectUtil.defaultIfNull(updateNull, true));
    }

    protected int updateBatch(List<M> models) throws BindException {
        return dtoClazz.equals(entityClazz)
                ? baseService.updateBatchById(models, true)
                : baseService.updateBatchById(models, dtoClazz, true);
    }

    protected List<M> list(Query query) {
        return dtoClazz.equals(entityClazz)
                ? baseService.queryList(query)
                : baseService.queryList(query, dtoClazz);
    }

    protected PagerView<M> page(PagerQuery query) {
        IPage<M> pager = dtoClazz.equals(entityClazz)
                ? baseService.queryPage(query)
                : baseService.queryPage(query, dtoClazz);
        return new PagerView<>(pager.getCurrent(), pager.getSize(), pager.getTotal(), pager.getRecords());
    }

    protected M detail(Serializable id) {
        return dtoClazz.equals(entityClazz)
                ? (M) baseService.getById(id)
                : (M) baseService.getById(id, dtoClazz);
    }

    protected int delete(M model) {
        Serializable id = EntityUtil.getPkVal(model, this.entityClazz);
        Assert.notNull(id, "无法获取主键值");
//        return baseService.removeById(id, mClazz); // 默认支持跨表删太危险，先改为仅删主表
        return baseService.removeById(id) ? 1 : 0;
    }

    protected int deleteBatch(List<M> models) {
        Set<Serializable> ids = new HashSet<>(models.size());
        for (int i = 0; i < models.size(); i++) {
            M model = models.get(i);
            Serializable id = EntityUtil.getPkVal(model, this.entityClazz);
            Assert.notNull(id, "第%d条数据无法获取主键值", i + 1);
            ids.add(id);
        }
//        return baseService.removeByIds(ids, mClazz); // 默认支持跨表删太危险，先改为仅删主表
        return baseService.removeByIds(ids) ? ids.size() : 0;
    }

    protected Boolean exists(List<Cond> conditions) {
        return dtoClazz.equals(entityClazz)
                ? baseService.exists(conditions)
                : baseService.exists(conditions, dtoClazz);
    }

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

    @ApiOperation(value = "下载/预览", notes = "针对上传的文件进行下载, 若是图片进行预览")
    @GetMapping("download")
    public void download(@RequestParam("path") String path, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        File file = baseService.download(path);
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            return;
        }
        try {
            String fileName = URLEncoder.encode(file.getName(), Charset.defaultCharset().toString());
            Optional<MediaType> optional = MediaTypeFactory.getMediaType(fileName);
            response.setContentType(optional.orElse(MediaType.APPLICATION_OCTET_STREAM).getType());
            response.setHeader("Connection", "close");
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));

            FileResourceHttpRequestHandler fileResourceHttpRequestHandler = SpringUtil.getBean(FileResourceHttpRequestHandler.class);
            request.setAttribute(FileResourceHttpRequestHandler.FILE_PATH, file.getAbsolutePath());
            fileResourceHttpRequestHandler.handleRequest(request, response);
        } catch (IOException | ServletException e) {
            throw e;
        }
    }

    @ApiOperation(value = "导出", notes = "数据导出")
    @PostMapping("export")
    public void export(@Validated @RequestBody ExportParam exportParam, HttpServletResponse response) throws IOException {
        List<M> data = exportParam.getAll() ? list(exportParam.getPageQuery()) : page(exportParam.getPageQuery()).getRecords();
        ExcelExportManager excelExportManager = new ExcelExportManager();
        try (ServletOutputStream out = response.getOutputStream()) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", String.format("attachment; filename=export.xlsx"));
            excelExportManager.exportByConfig(data, exportParam.getColumns(), out);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    protected ResponseEntity<FileSystemResource> responseFile(File file) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
//        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
//        headers.add("Pragma", "no-cache");
//        headers.add("Expires", "0");
//
//        MediaType mediaType = MediaTypeFactory.getMediaType(file.getName()).orElse(MediaType.APPLICATION_OCTET_STREAM);
//        return ResponseEntity.ok().headers(headers).contentType(mediaType).body(new FileSystemResource(file));
//    }
}
