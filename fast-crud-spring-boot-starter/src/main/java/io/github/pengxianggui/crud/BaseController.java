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
    public int insert(@RequestBody @Validated(CrudInsert.class) M model) {
        return dtoClazz.equals(entityClazz)
                ? baseService.insert(model)
                : baseService.insert(model, dtoClazz);
    }

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

    @ApiOperation("编辑")
    @PostMapping("update")
    public int update(@Validated(CrudUpdate.class) @RequestBody UpdateModelWrapper<M> modelWrapper) throws BindException {
        return dtoClazz.equals(entityClazz)
                ? baseService.update(modelWrapper.getModel()) ? 1 : 0
                : baseService.update(modelWrapper.getModel(), dtoClazz, ObjectUtil.defaultIfNull(modelWrapper.get_updateNull(), true));
    }

    @ApiOperation(value = "批量编辑", notes = "不支持个性化选择_updateNull")
    @PostMapping("update/batch")
    public int updateBatch(@RequestBody List<M> models) throws BindException {
        if (CollectionUtil.isEmpty(models)) return 0;
        for (M model : models) {
            ValidUtil.valid(validator, model, CrudUpdate.class);
        }
        return dtoClazz.equals(entityClazz)
                ? baseService.updateBatch(models) ? 1 : 0
                : baseService.updateBatch(models, dtoClazz, true);
    }

    @ApiOperation("列表查询")
    @PostMapping("list")
    public List<M> list(@RequestBody @Validated Query query) {
        return dtoClazz.equals(entityClazz)
                ? baseService.queryList(query)
                : baseService.queryList(query, dtoClazz);
    }

    @ApiOperation("分页查询")
    @PostMapping("page")
    public PagerView<M> page(@RequestBody @Validated PagerQuery query) {
        IPage<M> pager = dtoClazz.equals(entityClazz)
                ? baseService.queryPage(query)
                : baseService.queryPage(query, dtoClazz);
        return new PagerView<>(pager.getCurrent(), pager.getSize(), pager.getTotal(), pager.getRecords());
    }

    @ApiOperation("详情")
    @GetMapping("{id}/detail")
    public M detail(@PathVariable Serializable id) {
        return dtoClazz.equals(entityClazz)
                ? (M) baseService.getById(id)
                : (M) baseService.getById(id, dtoClazz);
    }

    @ApiOperation("删除")
    @PostMapping("delete")
    public int delete(@NotNull @RequestBody M model) {
        Serializable id = EntityUtil.getPkVal(model, this.entityClazz);
        Assert.notNull(id, "无法获取主键值");
//        return baseService.removeById(id, mClazz); // 默认支持跨表删太危险，先改为仅删主表
        return baseService.removeById(id) ? 1 : 0;
    }

    @ApiOperation("批量删除")
    @PostMapping("delete/batch")
    public int deleteBatch(@NotBlank @NotNull @RequestBody List<M> models) {
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

    @ApiOperation(value = "存在性查询", notes = "指定条件存在数据")
    @PostMapping("exists")
    public Boolean exists(@RequestBody List<Cond> conditions) {
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
}
