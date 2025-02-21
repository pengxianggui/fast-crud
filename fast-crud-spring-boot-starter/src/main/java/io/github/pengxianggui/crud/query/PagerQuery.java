package io.github.pengxianggui.crud.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("通用分页查询条件")
public class PagerQuery extends Query {

    @ApiModelProperty(value = "当前页,默认：1", example = "1")
    private long current = 1;

    @ApiModelProperty(value = "每页显示条数，默认： 20", example = "20")
    private long size = 20;

}
