package io.github.pengxianggui.crud;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pengxianggui.crud.download.FileResourceHttpRequestHandler;
import io.github.pengxianggui.crud.export.ExcelExportManager;
import io.github.pengxianggui.crud.query.Cond;
import io.github.pengxianggui.crud.query.ExportParam;
import io.github.pengxianggui.crud.query.PagerQuery;
import io.github.pengxianggui.crud.query.PagerView;
import io.github.pengxianggui.crud.query.Query;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * 只读 Controller 基类。继承此父类的 Controller 只提供查询、详情、存在性查询、下载与导出等只读接口。
 * <p>
 * 若业务表需要写能力，请继承 {@link BaseController}（兼容旧代码的全量基类）。
 * <p>
 * 注意: 继承此父类不是必须的，完全可以基于 service 自行提供 fast-table 所必须的接口。
 *
 * @param <M>
 * @author pengxg
 */
public class BaseReadController<M> {
    protected final BaseService baseService;
    protected final Class<M> dtoClazz;
    protected Class<?> entityClazz;
    @Autowired
    public Validator validator;
    @Autowired
    private ObjectMapper objectMapper;

    public BaseReadController(BaseService baseService, Class<M> dtoClazz) {
        this.baseService = baseService;
        this.dtoClazz = dtoClazz;
        this.entityClazz = baseService.getEntityClass();
    }

    /**
     * [FC] 查询列表
     *
     * @param query 查询条件
     * @return
     */
    @ApiOperation("列表查询")
    @PostMapping("list")
    public List<M> list(@RequestBody @Validated Query query) {
        return dtoClazz.equals(entityClazz)
                ? baseService.queryList(query)
                : baseService.queryList(query, dtoClazz);
    }

    /**
     * [FC] 分页查询
     *
     * @param query 查询条件
     * @return
     */
    @ApiOperation("分页查询")
    @PostMapping("page")
    public PagerView<M> page(@RequestBody @Validated PagerQuery query) {
        IPage<M> pager = dtoClazz.equals(entityClazz)
                ? baseService.queryPage(query)
                : baseService.queryPage(query, dtoClazz);
        return new PagerView<>(pager.getCurrent(), pager.getSize(), pager.getTotal(), pager.getRecords());
    }

    /**
     * [FC] 存在性查询
     *
     * @param conditions 条件
     * @return true-指定条件存在记录;false-指定条件不存在记录
     */
    @ApiOperation(value = "存在性查询", notes = "指定条件存在数据")
    @PostMapping("exists")
    public Boolean exists(@RequestBody @Validated List<Cond> conditions) {
        return dtoClazz.equals(entityClazz)
                ? baseService.exists(conditions)
                : baseService.exists(conditions, dtoClazz);
    }

    /**
     * [FC] 下载/预览
     *
     * @param path     路径
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
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

    /**
     * [FC] 表格数据导出
     *
     * @param exportParam 导出参数
     * @param response
     * @throws IOException
     */
    @ApiOperation(value = "导出", notes = "数据导出")
    @PostMapping("export")
    public void export(@RequestBody @Validated ExportParam exportParam, HttpServletResponse response) throws IOException {
        List<M> data = exportParam.getAll() ? list(exportParam.getPageQuery()) : page(exportParam.getPageQuery()).getRecords();
        ExcelExportManager excelExportManager = new ExcelExportManager(objectMapper);
        try (ServletOutputStream out = response.getOutputStream()) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", StrUtil.format("attachment; filename={}.xlsx",
                    StrUtil.blankToDefault(exportParam.getTitle(), "export")));
            excelExportManager.exportByConfig(data, exportParam.getColumns(), out);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
