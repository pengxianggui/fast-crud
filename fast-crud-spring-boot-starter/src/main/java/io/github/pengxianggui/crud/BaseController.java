package io.github.pengxianggui.crud;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import io.github.pengxianggui.crud.download.FileResourceHttpRequestHandler;
import io.github.pengxianggui.crud.meta.EntityUtil;
import io.github.pengxianggui.crud.query.*;
import io.github.pengxianggui.crud.valid.CrudInsert;
import io.github.pengxianggui.crud.valid.CrudUpdate;
import io.github.pengxianggui.crud.valid.ValidUtil;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;
import io.swagger.annotations.ApiOperation;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
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
import java.util.*;
import java.util.stream.Collectors;

public class BaseController<T> {
    private BaseService<T> baseService;
    public Validator validator;
    private String basePath;

    public BaseController(BaseService<T> baseService, Validator validator, String basePath) {
        this.baseService = baseService;
        this.validator = validator;
        basePath = StrUtil.nullToDefault(basePath, "");
        if (StrUtil.isNotBlank(basePath) && !basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }
        this.basePath = basePath;
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

    @ApiOperation(value = "上传", notes = "某个字段为图片/文件字段时需要使用上传接口")
    @PostMapping("upload")
    public String upload(@RequestParam("row") String row, @RequestParam("col") String col, MultipartFile file) throws IOException {
        String filePath = baseService.upload(row, col, file);
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            return filePath;
        }
        return this.basePath + "/download?path=" + URLEncoder.encode(filePath);
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
    public String export() {
        // TODO 支持传入表头的config, 以便导出时显示excel表头，以及一些option选项之类的
//        return responseFile(null)
        return null;
    }

    protected ResponseEntity<FileSystemResource> responseFile(File file) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        MediaType mediaType = MediaTypeFactory.getMediaType(file.getName()).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().headers(headers).contentType(mediaType).body(new FileSystemResource(file));
    }
}
