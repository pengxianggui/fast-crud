package io.github.pengxianggui.crud.batch;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 批量修改失败时的结构化错误响应。
 */
@Data
@Accessors(chain = true)
public class BatchUpdateError implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 固定为 false，便于前端统一判断 */
    private boolean success = false;

    /** 失败原因，可直接展示给用户 */
    private String message;

    /** 出错的接口路径 */
    private String path;
}
