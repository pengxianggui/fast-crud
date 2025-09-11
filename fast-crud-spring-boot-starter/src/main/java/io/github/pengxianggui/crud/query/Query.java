package io.github.pengxianggui.crud.query;

import io.github.pengxianggui.crud.query.validator.ValidQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
@ApiModel("通用查询条件")
@ValidQuery
public class Query {

    /**
     * 查询的字段, 为空时表示所有字段(依据dto声明的字段)
     */
    @ApiModelProperty("查询的字段")
    private List<String> cols;

    /**
     * 字段去重。指定cols时有效
     */
    @ApiModelProperty("字段去重。指定cols时有效")
    private boolean distinct;

    /**
     * 查询条件
     */
    @Valid
    @ApiModelProperty("查询条件")
    private List<Cond> conds;

    /**
     * 排序
     */
    @Valid
    @ApiModelProperty(value = "排序")
    private List<Order> orders;

    /**
     * 扩展字段
     */
    @ApiModelProperty(value = "扩展字段")
    private Map<String, Object> extra;

    public Query(String col, Object val) {
        this.cols = new ArrayList<>();
        this.conds = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.distinct = false;
        this.addCond(col, val);
    }

    public void addCond(String col, Object val) {
        addCond(col, Opt.EQ, val);
    }

    public void addCond(String col, Opt opt, Object val) {
        if (this.conds == null) {
            this.conds = new java.util.ArrayList<>();
        }
        this.conds.add(Cond.of(col, opt, val));
    }

    public <T> T getExtra(String fieldName, Class<T> clazz) {
        return getExtra(fieldName, (T) null);
    }

    public <T> T getExtra(String fieldName, T defaultVal) {
        if (this.extra == null || this.extra.isEmpty()) {
            return defaultVal;
        }
        return (T) this.extra.getOrDefault(fieldName, defaultVal);
    }
}
