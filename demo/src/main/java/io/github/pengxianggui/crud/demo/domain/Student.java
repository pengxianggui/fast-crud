package io.github.pengxianggui.crud.demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "Student对象", description = "学生")
public class Student {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("头像地址")
    private String avatarUrl;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("年龄")
    private Integer age;

    @ApiModelProperty("生日")
    private LocalDate birthday;

    @ApiModelProperty("性别")
    private String sex;

    @ApiModelProperty("属国")
    private String state;

    @ApiModelProperty("仰慕者id")
    private Integer loveId;

    @ApiModelProperty("仰慕者姓名")
    private String loveName;

    @ApiModelProperty("简介")
    private String info;

    @ApiModelProperty("已毕业")
    private Boolean graduated;

    @ApiModelProperty("幸运时刻")
    private LocalTime luckTime;

    @ApiModelProperty("简历地址")
    private String resumeUrl;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
