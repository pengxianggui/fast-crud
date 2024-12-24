package io.github.pengxianggui.crud.demo.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "Student对象", description = "学生")
public class Student {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String avatarUrl;

    private String name;

    private Integer age;

    // 单选
    private String sex;

    // 多选
    private String hobby;

    private String address;

    private Boolean graduated;

    private String luckTime;

    private LocalDate birthday;

    /**
     * 简历地址
     */
    private String resumeUrl;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
