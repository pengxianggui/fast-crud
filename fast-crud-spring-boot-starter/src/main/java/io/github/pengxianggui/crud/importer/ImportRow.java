package io.github.pengxianggui.crud.importer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导入的一行数据。绑定excel行号, 便于返回精确的行级错误。
 *
 * @param <T> 导入的目标对象类型(DTO或entity)
 * @author pengxg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportRow<T> {
    /**
     * excel行号(从1开始, 含表头行)
     */
    private int row;
    /**
     * 行数据
     */
    private T model;
}
