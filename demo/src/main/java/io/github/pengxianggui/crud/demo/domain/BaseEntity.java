package io.github.pengxianggui.crud.demo.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author pengxg
 * @date 2025/7/5 11:39
 */
@Data
public class BaseEntity extends IdEntity {
    @ApiModelProperty("创建人")
    private Long createBy;

    @ApiModelProperty("创建人姓名")
    private String createName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新人")
    private Long updateBy;

    @ApiModelProperty("更新人姓名")
    private String updateName;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("是否删除 0 否 1 是")
    private Boolean deleted;
}
