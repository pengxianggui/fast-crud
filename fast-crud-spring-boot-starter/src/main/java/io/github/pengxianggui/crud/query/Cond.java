package io.github.pengxianggui.crud.query;

import io.github.pengxianggui.crud.query.validator.ValidCond;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@ApiModel("分页条件")
@ValidCond
public class Cond {

    @NotBlank(message = "col字段值缺失, 请检查conds")
    @ApiModelProperty(value = "条件字段，当conds为空时有效")
    private String col;

    @ApiModelProperty(value = "字段值，当conds为空时有效")
    private Object val;

    @ApiModelProperty(value = "操作符，当conds为空时有效")
    private Opt opt = Opt.EQ;

    @ApiModelProperty(value = "条件关系")
    private Rel rel = Rel.AND;

    @Valid
    @ApiModelProperty(value = "嵌套条件")
    private List<Cond> conds;
}