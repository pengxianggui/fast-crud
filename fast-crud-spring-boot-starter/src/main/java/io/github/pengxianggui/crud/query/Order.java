package io.github.pengxianggui.crud.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@ApiModel("排序")
@Data
public class Order {

    @ApiModelProperty("列名")
    @NotBlank
    private String col;
    @ApiModelProperty("是否升序，默认：false")
    private boolean asc;

    public Order() {
    }

    public Order(String col, boolean asc) {
        this.col = col;
        this.asc = asc;
    }
}
