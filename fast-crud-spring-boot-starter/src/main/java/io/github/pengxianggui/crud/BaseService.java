package io.github.pengxianggui.crud;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.pengxianggui.crud.query.Cond;
import io.github.pengxianggui.crud.query.PagerQuery;
import io.github.pengxianggui.crud.query.Query;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface BaseService<T> extends IService<T> {
    /**
     * 列表查询
     *
     * @param query
     * @return
     */
    List<T> queryList(Query query);

    /**
     * 分页查询
     *
     * @param query
     * @return
     */
    IPage<T> queryPage(PagerQuery query);

    /**
     * 更新单条记录。
     * <p>
     * 与{@link #updateById(T entity)}不同的是, 此方法能支持指定此次是否更新null值字段, 若{@link UpdateModelWrapper#get_updateNull()}
     * 为null, 则等同于调用{@link #updateById(T entity)}
     *
     * @param entity     待更新的实体类
     * @param updateNull 是否更新null值字段
     * @return
     */
    boolean updateById(T entity, @Nullable Boolean updateNull);

    /**
     * 判断指定条件是否存在数据
     *
     * @param conditions 其中非null字段将参与条件进行筛选判断
     * @return
     */
    boolean exists(List<Cond> conditions);

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
     * 自定义跨表查询单个记录，若查询条件多个则返回第一个
     *
     * @param query
     * @param dtoClazz
     * @param <DTO>
     * @return
     */
    <DTO> DTO queryOne(Query query, Class<DTO> dtoClazz);

    /**
     * 跨表分页查询
     *
     * @param query    分页查询对象
     * @param dtoClazz 目标DTO类
     * @param <DTO>
     * @return
     */
    <DTO> IPage<DTO> queryPage(PagerQuery query, Class<DTO> dtoClazz);

    /**
     * 跨表详情查询
     *
     * @param id       主键
     * @param dtoClazz 目标DTO类
     * @param <DTO>
     * @return
     */
    <DTO> DTO getById(Serializable id, Class<DTO> dtoClazz);

    /**
     * 新增(支持跨表)
     *
     * @param model    待插入的对象
     * @param dtoClazz 待插入对象的类型
     * @param <DTO>
     * @return
     */
    <DTO> int insert(DTO model, Class<DTO> dtoClazz);

    /**
     * 批量新增(支持跨表)
     *
     * @param modelList  待插入的DTO列表
     * @param dtoClazz DTO类型
     * @param <DTO>
     * @return
     */
    <DTO> int insertBatch(Collection<DTO> modelList, Class<DTO> dtoClazz);

    /**
     * 更新(支持跨表)。注意一对多关联的子表不更新
     *
     * @param model      待更新的对象
     * @param dtoClazz   待更新对象的类型
     * @param updateNull 是否更新空值(null), 如果此值为null则表示不干预，由mybatis决定;
     * @param <DTO>
     * @return 返回更新数量
     */
    <DTO> int updateById(DTO model, Class<DTO> dtoClazz, @Nullable Boolean updateNull);

    /**
     * 批量更新(支持跨表)
     *
     * @param models     待更新对象列表
     * @param mClazz     待更新对象的类型
     * @param updateNull 是否更新空值(null), 如果此值为null则表示不干预，由mybatis决定;
     * @param <DTO>
     * @return
     */
    <DTO> int updateBatchById(List<DTO> models, Class<DTO> mClazz, @Nullable Boolean updateNull);

    /**
     * 删除(支持跨表) 谨慎! 会级联删除mClazz中@*Join声明关联的子表数据
     *
     * @param id     待删除的id
     * @param mClazz 待删除对象的类型
     * @param <DTO>
     * @return
     */
    <DTO> int removeById(Serializable id, Class<DTO> mClazz);

    /**
     * 批量删除(支持跨表) 谨慎! 会级联删除mClazz中@*Join声明关联的子表数据
     *
     * @param ids    待删除的id集合
     * @param mClazz 待删除对象的类型
     * @param <DTO>
     * @return
     */
    <DTO> int removeByIds(Collection<? extends Serializable> ids, Class<DTO> mClazz);

    /**
     * 判断指定条件是否存在数据(支持跨表)
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
