package io.github.pengxianggui.crud.batch;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 批量修改的返回结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "BatchUpdateResult", description = "批量修改结果")
public class BatchUpdateResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 实际受影响行数。值与原值相同的行不计入，被数据权限过滤的行也不计入，
     * 因此可能小于 requestedRows。
     */
    @ApiModelProperty("实际更新(受影响)的行数")
    private Integer affectedRows;

    /** 本次请求提交的主键数量 */
    @ApiModelProperty("本次提交的记录数")
    private Integer requestedRows;

    /** 实际被修改的字段（实体属性名） */
    @ApiModelProperty("本次被修改的字段(实体属性名)")
    private List<String> fields;

    /** 实际执行的 UPDATE 语句条数，恒为 1 */
    @ApiModelProperty("实际执行的UPDATE语句条数")
    private Integer executedStatements;

    public static BatchUpdateResult of(int affectedRows, int requestedRows,
                                       List<String> fields, int executedStatements) {
        return new BatchUpdateResult(affectedRows, requestedRows, fields, executedStatements);
    }
}
