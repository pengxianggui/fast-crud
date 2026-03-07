package io.github.pengxianggui.crud.query;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
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
    private List<String> cols = new ArrayList<>();

    /**
     * 字段去重。指定cols时有效
     */
    @ApiModelProperty("字段去重。指定cols时有效")
    private boolean distinct = false;

    /**
     * 查询条件
     */
    @Valid
    @ApiModelProperty("查询条件")
    private List<Cond> conds = new ArrayList<>();

    /**
     * 排序
     */
    @Valid
    @ApiModelProperty(value = "排序")
    private List<Order> orders = new ArrayList<>();

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

    public void removeCond(String col) {
        if (this.conds == null) {
            return;
        }
        this.conds.removeIf(cond -> col.equals(cond.getCol()));
    }

    /**
     * 获取指定条件的值
     *
     * @param col   字段名
     * @param opt   比较符
     * @param clazz 值类型
     * @param <T>
     * @return
     */
    public <T> T getCondVal(String col, Opt opt, Class<T> clazz) {
        Cond cond = getCond(col, opt);
        return cond == null ? null : Convert.convert(clazz, cond.getVal());
    }

    /**
     * 获取指定条件
     *
     * @param col 字段名
     * @param opt 比较符
     * @return
     */
    public Cond getCond(String col, Opt opt) {
        Assert.notNull(col);
        Assert.notNull(opt);
        if (this.conds == null) {
            return null;
        }
        return this.conds.stream().filter(cond -> col.equals(cond.getCol()) && opt.equals(cond.getOpt()))
                .findFirst().orElse(null);
    }

    public <T> T getExtra(String fieldName, Class<T> clazz) {
        return getExtra(fieldName, (T) null);
    }

    public <T> T getExtra(String fieldName, T defaultVal) {
        if (this.extra == null || !this.extra.containsKey(fieldName)) {
            return defaultVal;
        }
        Object value = this.extra.get(fieldName);
        if (value == null) {
            return defaultVal;
        }
        if (defaultVal == null) {
            return null;
        }
        // 根据默认值的类型进行自动转换
        return (T) Convert.convert(defaultVal.getClass(), value);
    }
}
