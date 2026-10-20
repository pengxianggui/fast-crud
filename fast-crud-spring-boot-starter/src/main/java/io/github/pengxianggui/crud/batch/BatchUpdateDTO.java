package io.github.pengxianggui.crud.batch;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 批量修改请求体，把勾选的多行记录的指定字段统一改为指定值。
 * <pre>
 * {
 *   "ids": [1, 2],
 *   "fields": [
 *     {"field": "sex",   "value": "0"},
 *     {"field": "state", "value": "3"}
 *   ]
 * }
 * </pre>
 * 取值编码与 demo 前端字典一致：性别 男=1 女=0；属国 魏=1 蜀=2 吴=3 汉=4 晋=5 无=0。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "BatchUpdateDTO", description = "批量修改请求体")
public class BatchUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 待修改的记录主键集合，即页面上勾选的那些行。
     */
    @ApiModelProperty(value = "待修改记录的主键集合", required = true, example = "[1,2]")
    @NotEmpty(message = "请至少选择一条待修改的记录")
    private List<Serializable> ids;

    /**
     * 要修改的字段及目标值，可同时提交多组。
     */
    @ApiModelProperty(value = "要修改的字段及目标值", required = true)
    @NotEmpty(message = "请至少添加一个待修改的字段")
    @Valid
    private List<FieldValue> fields;

    /**
     * 单个「字段 = 值」。
     * 字段名必须是实体属性名（camelCase，如 sex、state），不接受数据库列名，
     * 白名单校验见 {@link BatchUpdateFieldRegistry}。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "BatchUpdateDTO.FieldValue", description = "字段与目标值")
    public static class FieldValue implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 实体属性名（camelCase），例如 sex、state、graduated。
         */
        @ApiModelProperty(value = "实体属性名", required = true, example = "sex")
        @NotEmpty(message = "字段名不能为空")
        private String field;

        /**
         * 目标值。允许为 null，此时等价于把该字段置空；是否真的置空由 allowNull 决定。
         */
        @ApiModelProperty(value = "目标值", example = "0")
        private Object value;

        /**
         * 是否允许把该字段置为 null。默认 false —— 显式开启才允许清空，
         * 避免前端漏传 value 时把整列数据静默清空。
         */
        @ApiModelProperty(value = "是否允许置空(null)", example = "false")
        private boolean allowNull = false;

        public FieldValue(String field, Object value) {
            this.field = field;
            this.value = value;
            this.allowNull = false;
        }
    }
}
