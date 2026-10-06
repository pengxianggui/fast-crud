package io.github.pengxianggui.crud.importer;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * excel解析结果。
 *
 * @param <T> 导入的目标对象类型
 * @author pengxg
 */
@Data
@AllArgsConstructor
public class ImportParseResult<T> {
    /**
     * 解析成功的行
     */
    private List<ImportRow<T>> rows = new ArrayList<>();
    /**
     * 解析失败的行错误
     */
    private List<ImportError> errors = new ArrayList<>();
}
