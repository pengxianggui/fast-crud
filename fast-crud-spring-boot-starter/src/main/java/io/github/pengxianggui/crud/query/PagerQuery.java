package io.github.pengxianggui.crud.query;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("通用分页查询条件")
public class PagerQuery extends Query {

    @ApiModelProperty(value = "当前页,默认：1", example = "1")
    private long current = 1;

    @ApiModelProperty(value = "每页显示条数，默认： 20", example = "20")
    private long size = 20;

    public <P> Pager<P> toPager() {
        Pager<P> p = new Pager<P>();
        p.setCurrent(this.current);
        p.setDistinct(this.isDistinct());
        p.setSize(this.size);
        p.setCols(this.getCols());
        p.setConds(this.conds);

        if (this.orders != null) {
            this.orders.forEach((order) -> {
                if (order != null) {
                    if (order.isAsc()) {
                        p.orders().add(OrderItem.asc(order.getCol()));
                    } else {
                        p.orders().add(OrderItem.desc(order.getCol()));
                    }
                }
            });
        }
        return p;
    }

    public <P> PagerView<P> toView() {
        PagerView<P> p = new PagerView<>();
        p.setCurrent(this.current);
        p.setSize(this.size);
        return p;
    }

    public <P> PagerView<P> toView(List<P> records, long total) {
        PagerView<P> p = new PagerView<>();
        p.setCurrent(this.current);
        p.setSize(this.size);
        p.setTotal(total);
        p.setRecords(records);
        return p;
    }
}
