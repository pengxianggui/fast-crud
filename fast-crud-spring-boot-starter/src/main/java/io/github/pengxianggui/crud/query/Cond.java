package io.github.pengxianggui.crud.query;

import io.github.pengxianggui.crud.query.validator.ValidCond;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@ApiModel("查询条件")
@ValidCond
public class Cond {

    /**
     * 字段名，当conds无内容时有效
     */
    @NotBlank(message = "col字段值缺失, 请检查conds")
    @ApiModelProperty(value = "条件字段，当conds为空时有效")
    private String col;

    /**
     * 操作符，默认为EQ(=)，当conds无内容时有效
     */
    @ApiModelProperty(value = "操作符，当conds为空时有效")
    private Opt opt = Opt.EQ;

    /**
     * 字段值，当conds无内容时有效
     */
    @ApiModelProperty(value = "字段值，当conds为空时有效")
    private Object val;

    /**
     * 条件关系(当conds有内容时生效)
     */
    @ApiModelProperty(value = "条件关系")
    private Rel rel = Rel.AND;

    /**
     * 嵌套条件(若此值有内容, 则conds和rel生效，其余字段无效)
     */
    @Valid
    @ApiModelProperty(value = "嵌套条件(若此值有内容, 则conds和rel生效，其余字段无效)")
    private List<Cond> conds;

    public static Cond of(String col, Opt opt, Object val) {
        Cond cond = new Cond();
        cond.setCol(col);
        cond.setOpt(opt);
        cond.setVal(val);
        return cond;
    }

    public static Cond of(Rel rel, List<Cond> conds) {
        Cond cond = new Cond();
        cond.setRel(rel);
        cond.setConds(conds);
        return cond;
    }
}