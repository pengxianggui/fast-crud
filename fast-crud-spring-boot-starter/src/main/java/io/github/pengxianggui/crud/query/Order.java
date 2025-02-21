package io.github.pengxianggui.crud.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@ApiModel("排序")
@Data
public class Order {
    @ApiModelProperty("列名")
    @NotBlank(message = "col字段值缺失, 请检查orders")
    private String col;

    @ApiModelProperty("是否升序，默认：false")
    private boolean asc;
}
