package io.github.pengxianggui.crud.batch;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量修改的审计记录。
 * 记录可复现该次操作的信息：谁、何时、哪些主键、把哪些字段改成了什么、命中多少行、几条 SQL。
 * 逐行记录变更前值成本过高，需要前后对照时可配合业务表自身的 update_time/update_by 回溯。
 */
@Data
@Accessors(chain = true)
public class BatchUpdateAudit {

    /** 实体类全名 */
    private String entityName;

    /** 实际被更新的主键集合 */
    private List<Serializable> ids;

    /** 提交的主键数量，可能大于 ids.size()（有行被数据权限过滤） */
    private Integer requestedRows;

    /** 字段名 -> 新值（实体属性名） */
    private Map<String, Object> newValues = new LinkedHashMap<>();

    /** 被修改的字段名列表 */
    private List<String> fields;

    /** 实际受影响行数 */
    private Integer affectedRows;

    /** 实际执行的 UPDATE 语句条数 */
    private Integer executedStatements;

    /** 操作人，需业务自行回填 */
    private String operator;

    /** 扩展信息，需业务自行回填 */
    private Map<String, Object> extra;

    /** 操作时间 */
    private LocalDateTime operatedAt = LocalDateTime.now();
}
