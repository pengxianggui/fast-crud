package io.github.pengxianggui.crud.demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author pengxg
 * @date 2025/7/5 11:46
 */
@Data
public class IdEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
}
