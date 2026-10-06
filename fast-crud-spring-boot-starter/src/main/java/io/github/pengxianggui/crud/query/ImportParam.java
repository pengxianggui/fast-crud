package io.github.pengxianggui.crud.query;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导入参数。与{@link ExportParam}对标, 作为导入请求body中的json部分提交(part名: param), 文件通过part名file提交。
 *
 * @author pengxg
 */
@Data
public class ImportParam {
    /**
     * 导入列的元数据画像, 与导出列元数据同构(即{@link ExportParam#getColumns()}), 例如:
     * <pre>
     *      {
     *             "col": "sex",
     *             "label": "性别",
     *             "importable": true,   // 是否参与导入
     *             "importUnique": false, // 是否作为无主键时的匹配键
     *             "tableColumnComponentName": "fast-table-column-select",
     *             "props": {
     *                 "labelKey": "label",
     *                 "valKey": "value",
     *                 "options": [
     *                     {"label": "男", "value": "1"},
     *                     {"label": "女", "value": "0"}
     *                 ]
     *             }
     *      }
     * </pre>
     */
    @NotEmpty
    private List<Map<String, Object>> columns;
    /**
     * 额外参数, 如{"customerId": 1}。业务可在beforeImport钩子中获取
     */
    private Map<String, Object> extra = new HashMap<>();
}
