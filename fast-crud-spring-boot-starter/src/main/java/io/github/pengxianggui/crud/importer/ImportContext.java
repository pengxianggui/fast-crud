package io.github.pengxianggui.crud.importer;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导入上下文。作为{@code beforeImport}钩子的入参, 业务可在其中回填额外参数(如customerId)或做自定义校验。
 *
 * @param <T> 导入的目标对象类型(DTO或entity)
 * @author pengxg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportContext<T> {
    /**
     * 全部导入行(含excel行号)。业务可直接修改其中的model
     */
    private List<ImportRow<T>> rows;
    /**
     * 导入的目标对象类型
     */
    private Class<T> dtoClazz;
    /**
     * 导入列配置(与导出列元数据同构), 其中importUnique=true的列将作为无主键时的匹配键
     */
    private List<Map<String, Object>> columns;
    /**
     * 前端额外传入的参数(如customerId)
     */
    private Map<String, Object> extra;

    /**
     * 获取所有行数据(不含行号)
     */
    public List<T> getModels() {
        if (rows == null) {
            return Collections.emptyList();
        }
        return rows.stream().map(ImportRow::getModel).collect(Collectors.toList());
    }

    /**
     * 获取被标记为importUnique的字段名列表
     */
    public List<String> getImportUniqueCols() {
        if (columns == null) {
            return Collections.emptyList();
        }
        List<String> cols = new ArrayList<>();
        for (Map<String, Object> column : columns) {
            if (Boolean.TRUE.equals(column.get("importUnique")) && StrUtil.isNotBlank((String) column.get("col"))) {
                cols.add((String) column.get("col"));
            }
        }
        return cols;
    }
}
