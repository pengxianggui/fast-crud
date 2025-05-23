package io.github.pengxianggui.crud;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.pengxianggui.crud.query.Cond;
import io.github.pengxianggui.crud.query.Pager;
import io.github.pengxianggui.crud.query.PagerQuery;
import io.github.pengxianggui.crud.query.Query;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface BaseService<T> extends IService<T> {

    /**
     * 自定义列表查询
     *
     * @param query
     * @return
     */
    List<T> queryList(Query query);

    /**
     * 自定义跨表列表查询
     *
     * @param query    查询对象
     * @param dtoClazz 目标DTO类
     * @param <DTO>
     * @return
     */
    <DTO> List<DTO> queryList(Query query, Class<DTO> dtoClazz);

    /**
     * 自定义分页查询
     *
     * @param query
     * @return
     */
    Pager<T> queryPage(PagerQuery query);

    /**
     * 跨表分页查询
     *
     * @param query    分页查询对象
     * @param dtoClazz 目标DTO类
     * @param <DTO>
     * @return
     */
    <DTO> Pager<DTO> queryPage(PagerQuery query, Class<DTO> dtoClazz);

    /**
     * 与{@link #updateById(T entity)}不同的是, 此方法能支持指定此次是否更新null值字段, 若{@link UpdateModelWrapper#get_updateNull()}
     * 为null, 则等同于调用{@link #updateById(T entity)}
     *
     * @param modelWrapper
     * @return
     */
    boolean updateById(UpdateModelWrapper<T> modelWrapper);

    /**
     * 与{@link #updateById(T entity)} 不同的是，此方法支持跨表更新。
     *
     * @param dtoWrapper
     * @param dtoClazz   目标DTO类
     * @param <DTO>
     * @return
     */
    <DTO> boolean updateById(UpdateModelWrapper<DTO> dtoWrapper, Class<DTO> dtoClazz);

    /**
     * 跨表删除。从表会一并删除
     *
     * @param ids
     * @param dtoClazz
     * @param <DTO>
     * @return
     */
    <DTO> boolean deleteByIds(Collection<?> ids, Class<DTO> dtoClazz);

    /**
     * 判断指定条件是否存在数据
     *
     * @param conditions 其中非null字段将参与条件进行筛选判断
     * @return
     */
    boolean exists(List<Cond> conditions);

    /**
     * 跨表判断指定条件是否存在数据
     *
     * @param conditions 其中非null字段将参与条件进行筛选判断
     * @param dtoClazz   目标DTO类
     * @param <DTO>
     * @return
     */
    <DTO> boolean exists(List<Cond> conditions, Class<DTO> dtoClazz);

    /**
     * 针对某个字段上传文件。默认将上传到java临时目录。如果希望自定义可以通过fast-crud.upload-dir配置
     *
     * @param row  所在行记录
     * @param col  所在列字段(java属性)
     * @param file 上传的文件
     * @return 返回上传的url, 这个url应当是前端可以访问的路径
     */
    String upload(String row, String col, MultipartFile file) throws IOException;

    /**
     * 下载文件
     *
     * @param path 指定文件路径
     * @return
     */
    File download(String path);
}
