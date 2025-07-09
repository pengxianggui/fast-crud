package io.github.pengxianggui.crud.demo.controller.vo;

import io.github.pengxianggui.crud.demo.domain.Student;
import io.github.pengxianggui.crud.demo.domain.StudentSensitive;
import io.github.pengxianggui.crud.file.FileItem;
import io.github.pengxianggui.crud.join.JoinMain;
import io.github.pengxianggui.crud.join.LeftJoin;
import io.github.pengxianggui.crud.join.OnCond;
import io.github.pengxianggui.crud.join.RelateTo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 学生分页视图模型
 *
 * @author pengxg
 * @date 2025/7/9 10:16
 */
@Data
@JoinMain(Student.class)
@LeftJoin(value = StudentSensitive.class, on = {@OnCond(field = "studentId", targetField = "id")})
public class StudentPageVO {

    private Integer id;

    @ApiModelProperty("头像地址")
    private String avatarUrl;

    @ApiModelProperty("相册")
    private List<FileItem> gallery;

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
    private List<FileItem> resumeUrl;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    // ------------------ 关联子表
    @ApiModelProperty("身份证号")
    @RelateTo(value = StudentSensitive.class)
    private String idCard;

    @ApiModelProperty("地址")
    @RelateTo(value = StudentSensitive.class)
    private String address;

    @ApiModelProperty("联系电话")
    @RelateTo(value = StudentSensitive.class)
    private String phone;
}
