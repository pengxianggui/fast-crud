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
    @NotEmpty
    private List<Map<String, Object>> columns;
    /**
     * 限定数据过滤条件
     */
    private PagerQuery pageQuery;
    /**
     * true则为全部, false则为当前页(pageQuery.current, pageQuery.size)
     */
    private Boolean all = true;

    /**
     * 勾选导出的主键集合。非null时优先于all:
     * 空集合导出0行; 非空集合在pageQuery原有条件下追加主键IN条件后导出
     */
    private List<Object> selectedIds;

    /**
     * 勾选导出使用的主键字段名，需与页面FastTableOption.idField一致
     */
    private String idField;

    /**
     * 是否为下载空模板。为true时仅输出表头(连同下拉校验等), 不输出任何数据行。用于导出导入模板
     */
    private Boolean template = false;

    private String title;
}
