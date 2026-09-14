package io.github.pengxianggui.crud.wrapper;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 批量修改参数。
 * <p>
 *  * 所有选中行应用相同的字段值，适合选多行统一改几个字段的场景。
 */
@Data
@ApiModel(description = "批量修改参数")
public class BatchUpdateFieldModel {

    @ApiModelProperty(value = "主键id集合", required = true)
    @NotEmpty(message = "ids不能为空")
    private List<? extends Serializable> ids;

    @ApiModelProperty(value = "需要修改的字段及值, key为字段名, value为字段值", required = true)
    @NotEmpty(message = "fields不能为空")
    private Map<String, Object> fields;
}