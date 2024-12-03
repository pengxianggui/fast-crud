package io.github.pengxianggui.crud.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class Pager<T> extends Page<T> {

    @ApiModelProperty("是否唯一")
    private boolean distinct;

    @ApiModelProperty("查询的字段")
    String[] cols;

    @ApiModelProperty("查询条件")
    private List<Cond> conds;

    public PagerView<T> toView() {
        PagerView<T> p = new PagerView<>();
        p.setCurrent(this.getCurrent());
        p.setSize(this.getSize());
        p.setTotal(this.getTotal());
        p.setRecords(this.getRecords());
        return p;
    }

    public QueryWrapper<T> wrapper() {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        //条件
        QueryWrapperUtil.addConditions(wrapper, this.conds);

        //排序
        QueryWrapperUtil.addOrders(wrapper, this.orders());

        //查询字段
        QueryWrapperUtil.addSelect(wrapper, this.cols, this.distinct);

        return wrapper;
    }

    public QueryWrapper<T> count() {
        //条件，用于select count
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        QueryWrapperUtil.addConditions(wrapper, this.conds);
        return wrapper;
    }
}
