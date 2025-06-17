package io.github.pengxianggui.crud.dao;

import com.github.yulichang.base.MPJBaseMapper;

/**
 * 继承此接口，默认会继承MPJBaseMapper，无需再继承mybatis-plus中的BaseMapper
 *
 * @author pengxg
 * @date 2025/5/22 09:13
 */
public interface BaseMapper<T> extends MPJBaseMapper<T> {
}
