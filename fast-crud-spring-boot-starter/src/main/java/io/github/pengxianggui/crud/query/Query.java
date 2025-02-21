package io.github.pengxianggui.crud.query;

import io.github.pengxianggui.crud.query.validator.ValidQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import java.util.List;

@Getter
@Setter
@ApiModel("通用查询条件")
@ValidQuery
public class Query {

    @ApiModelProperty("查询的字段")
    private List<String> cols;

    @ApiModelProperty("字段去重。指定cols时有效")
    private boolean distinct;

    @Valid
    @ApiModelProperty("查询条件")
    private List<Cond> conds;

    @Valid
    @ApiModelProperty(value = "排序")
    private List<Order> orders;

}
