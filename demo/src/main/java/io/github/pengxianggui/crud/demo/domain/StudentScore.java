package io.github.pengxianggui.crud.demo.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("student_score")
@ApiModel(value = "StudentScore对象", description = "学生成绩")
public class StudentScore extends IdEntity {

    @ApiModelProperty("学生id")
    private Long studentId;

    @ApiModelProperty("学科")
    private String subject;

    @ApiModelProperty("得分")
    private BigDecimal score;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
