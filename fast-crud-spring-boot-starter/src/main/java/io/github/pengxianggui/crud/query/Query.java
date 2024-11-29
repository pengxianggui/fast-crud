package io.github.pengxianggui.crud.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.pengxianggui.crud.query.validator.ValidQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import java.util.List;

@ApiModel("通用查询条件")
@ValidQuery
public class Query {

    @Getter
    @Setter
    @ApiModelProperty("查询的字段")
    private String[] cols;

    @Getter
    @Setter
    @ApiModelProperty("是否唯一")
    private boolean distinct;

    @Getter
    @Setter
    @Valid
    @ApiModelProperty("查询条件")
    List<Cond> conds;

    @Getter
    @Setter
    @Valid
    @ApiModelProperty(value = "排序")
    List<Order> orders;

    public <T> QueryWrapper<T> wrapper() {
        QueryWrapper<T> wrapper = new QueryWrapper<T>();
        //条件
        QueryWrapperUtil.addConditions(wrapper, this.conds);

        //排序
        QueryWrapperUtil.addQueryOrders(wrapper, this.orders);

        //查询字段
        QueryWrapperUtil.addSelect(wrapper, this.cols, this.distinct);
        return wrapper;
    }
}
