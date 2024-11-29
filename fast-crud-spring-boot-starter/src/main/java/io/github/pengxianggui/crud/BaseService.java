package io.github.pengxianggui.crud;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.pengxianggui.crud.wrapper.UpdateModelWrapper;

public interface BaseService<T> extends IService<T> {

    boolean updateById(UpdateModelWrapper<T> modelWrapper);
}
