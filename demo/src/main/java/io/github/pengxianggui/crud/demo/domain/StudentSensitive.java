package io.github.pengxianggui.crud.demo.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("student_sensitive")
@ApiModel(value = "StudentSensitive对象", description = "学生-敏感信息子表")
public class StudentSensitive extends IdEntity {

    @ApiModelProperty("学生id")
    private Long studentId;

    @ApiModelProperty("身份证号")
    private String idCard;

    @ApiModelProperty("地址")
    private String address;

    @ApiModelProperty("联系电话")
    private String phone;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
