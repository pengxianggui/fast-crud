package io.github.pengxianggui.crud.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.pengxianggui.crud.query.validator.ValidCond;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("分页条件")
@ValidCond
public class Cond<T> {

    @ApiModelProperty(value = "条件字段，当conds为空时有效")
    private String col;

    @ApiModelProperty(value = "字段值，当conds为空时有效")
    private Object val;

    public Object getVal() {
        if (this.val != null && this.val instanceof Long && this.col != null) {
            if (this.col.equals("created") || this.col.equals("updated") || this.col.endsWith("_time")) {
                return new Date((Long) this.val);
            }
        }
        return this.val;
    }

    @ApiModelProperty(value = "操作符，当conds为空时有效")
    private Operation opt = Operation.EQ;

    @ApiModelProperty(value = "条件关系")
    private Relation rel = Relation.AND;

    @Valid
    @ApiModelProperty(value = "嵌套条件")
    private List<Cond> conds;

    public enum Operation {
        EQ("="), NE("!="), GT(">"), GE(">="), LT("<"), LE("<="), IN("in"), NIN("nin"), LIKE("like"), NLIKE("nlike"), NULL("null"), NNULL("nnull");

        private String value;

        Operation(String value) {
            this.value = value;
        }

        @JsonCreator
        public static Operation getOperationByValue(String value) {
            for (Operation opt : Operation.values()) {
                if (opt.value.equals(value)) {
                    return opt;
                }
            }
            return null;
        }

        @JsonValue
        public String getValue() {
            return this.value;
        }

    }

    public enum Relation {
        AND("and"), OR("or");

        private String value;

        Relation(String value) {
            this.value = value;
        }

        @JsonCreator
        public static Relation getRelationByValue(String value) {
            for (Relation rel : Relation.values()) {
                if (rel.value.equals(value)) {
                    return rel;
                }
            }
            return null;
        }

        @JsonValue
        public String getValue() {
            return this.value;
        }
    }
}