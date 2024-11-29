package io.github.pengxianggui.domain.demo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "Test对象", description = "")
public class Test {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer age;
}
