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

    public QueryWrapper<T> wrapper(Class<T> entityClazz) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        //条件
        QueryWrapperUtil.addConditions(wrapper, this.conds, entityClazz);

        //排序
        QueryWrapperUtil.addOrders(wrapper, this.orders(), entityClazz);
        this.orders.clear(); // important: 否则不仅排序条件重复了, 而且this.orders中的col是类属性名，而非数据库字段名

        //查询字段
        QueryWrapperUtil.addSelect(wrapper, this.cols, this.distinct, entityClazz);

        return wrapper;
    }

    public QueryWrapper<T> count(Class<T> entityClazz) {
        //条件，用于select count
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        QueryWrapperUtil.addConditions(wrapper, this.conds, entityClazz);
        return wrapper;
    }
}
