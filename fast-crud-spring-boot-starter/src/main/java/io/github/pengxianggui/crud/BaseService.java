package io.github.pengxianggui.crud;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.pengxianggui.crud.query.Cond;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;

import java.util.List;

public interface BaseService<T> extends IService<T> {

    /**
     * 与{@link #updateById(T entity)}不同的是, 此方法能支持指定此次是否更新null值字段, 若{@link UpdateModelWrapper#get_updateNull()}
     * 为null, 则等同于调用{@link #updateById(T entity)}
     *
     * @param modelWrapper
     * @return
     */
    boolean updateById(UpdateModelWrapper<T> modelWrapper);

    /**
     * 判断指定条件是否存在数据
     *
     * @param conditions 其中非null字段将参与条件进行筛选判断
     * @return
     */
    boolean exists(List<Cond> conditions);
}
