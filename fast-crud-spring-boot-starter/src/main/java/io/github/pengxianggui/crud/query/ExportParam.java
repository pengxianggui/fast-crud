package io.github.pengxianggui.crud.query;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * @author pengxg
 * @date 2025/4/30 14:20
 */
@Data
public class ExportParam {
    /**
     * 导出列的元数据画像, 例如:
     * <pre>
     *      {
     *             "col": "sex",
     *             "exportable": true,
     *             "label": "性别",
     *             "props":
     *             {
     *                 "filter": true,
     *                 "firstFilter": false,
     *                 "label": "性别",
     *                 "labelKey": "label",
     *                 "minWidth": "90px",
     *                 "options":
     *                 [
     *                     {
     *                         "label": "男",
     *                         "value": "1"
     *                     },
     *                     {
     *                         "label": "女",
     *                         "value": "0"
     *                     }
     *                 ],
     *                 "prop": "sex",
     *                 "quickFilterBlock": false,
     *                 "quickFilterCheckbox": false,
     *                 "showOverflowTooltip": false,
     *                 "size": "medium",
     *                 "valKey": "value"
     *             },
     *             "tableColumnComponentName": "fast-table-column-select"
     *         }
     * </pre>
     */
    @NotEmpty(message = "无任何导出列")
    private List<Map<String, Object>> columns;
    /**
     * 限定数据过滤条件
     */
    private PagerQuery pageQuery;
    /**
     * true则为全部, false则为当前页(pageQuery.current, pageQuery.size)
     */
    private Boolean all = true;

    private String title;
}
