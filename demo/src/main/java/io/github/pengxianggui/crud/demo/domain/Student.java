package io.github.pengxianggui.crud.demo.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.pengxianggui.crud.dao.typehandler.FileItemTypeHandler;
import io.github.pengxianggui.crud.file.FileItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "Student对象", description = "学生")
@TableName(value = "student", autoResultMap = true)
public class Student extends IdEntity {

    @ApiModelProperty("头像地址")
    private String avatarUrl;

    @TableField(typeHandler = FileItemTypeHandler.class, jdbcType = JdbcType.VARCHAR, updateStrategy = FieldStrategy.ALWAYS)
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
    @TableField(typeHandler = FileItemTypeHandler.class, jdbcType = JdbcType.VARCHAR)
    private List<FileItem> resumeUrl;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
