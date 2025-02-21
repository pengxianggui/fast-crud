package io.github.pengxianggui.crud.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@ApiModel("通用分页返回")
public class PagerView<T> {

    @ApiModelProperty("当前页")
    private long current = 1;

    @ApiModelProperty("每页显示条数，默认 10")
    private long size = 10;

    @ApiModelProperty("总数")
    private long total = 0;

    @ApiModelProperty("记录")
    protected List<T> records = new ArrayList<>();

    public PagerView(long current, long size, long total, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.records = records;
    }

}
