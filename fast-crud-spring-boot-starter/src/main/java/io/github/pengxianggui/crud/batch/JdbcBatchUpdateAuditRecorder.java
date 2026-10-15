package io.github.pengxianggui.crud.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.LinkedHashMap;

/**
 * 基于 {@link JdbcTemplate} 的默认审计实现，把批量修改记录写入 fc_batch_update_log 表。
 * 建表语句由本类在启动时自动执行（幂等），避免要求使用方手工维护 DDL；
 * 表名、是否自动建表可通过构造参数控制。
 */
@Slf4j
public class JdbcBatchUpdateAuditRecorder implements BatchUpdateAuditRecorder {

    private static final String DEFAULT_TABLE = "fc_batch_update_log";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final String table;
    private final boolean autoCreateTable;

    public JdbcBatchUpdateAuditRecorder(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this(jdbcTemplate, objectMapper, DEFAULT_TABLE, true);
    }

    public JdbcBatchUpdateAuditRecorder(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper,
                                        String table, boolean autoCreateTable) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.table = table;
        this.autoCreateTable = autoCreateTable;
    }

    @PostConstruct
    public void init() {
        if (!autoCreateTable) {
            return;
        }
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `" + table + "` ("
                    + "  `id` BIGINT NOT NULL AUTO_INCREMENT,"
                    + "  `entity_name` VARCHAR(255) NOT NULL COMMENT '实体类全名',"
                    + "  `ids` TEXT COMMENT '被修改的主键集合(json)',"
                    + "  `fields` VARCHAR(1000) COMMENT '被修改的字段(逗号分隔)',"
                    + "  `new_values` TEXT COMMENT '字段新值(json)',"
                    + "  `requested_rows` INT COMMENT '提交的记录数',"
                    + "  `affected_rows` INT COMMENT '实际受影响行数',"
                    + "  `executed_statements` INT COMMENT '执行的UPDATE语句条数',"
                    + "  `operator` VARCHAR(128) COMMENT '操作人',"
                    + "  `extra` TEXT COMMENT '扩展信息(json)',"
                    + "  `operated_at` DATETIME COMMENT '操作时间',"
                    + "  PRIMARY KEY (`id`),"
                    + "  KEY `idx_entity_operated_at` (`entity_name`, `operated_at`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='fast-crud 批量修改审计日志'");
            log.debug("Batch update audit table `{}` is ready.", table);
        } catch (Exception e) {
            log.warn("Failed to create batch update audit table `{}`, "
                    + "batch update audit will be skipped. Cause: {}", table, e.getMessage());
        }
    }

    @Override
    public void record(BatchUpdateAudit audit) {
        if (audit == null) {
            return;
        }
        try {
            jdbcTemplate.update("INSERT INTO `" + table + "` "
                            + "(`entity_name`,`ids`,`fields`,`new_values`,`requested_rows`,"
                            + "`affected_rows`,`executed_statements`,`operator`,`extra`,`operated_at`) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?)",
                    audit.getEntityName(),
                    toJson(audit.getIds()),
                    audit.getFields() == null ? null : String.join(",", audit.getFields()),
                    toJson(audit.getNewValues()),
                    audit.getRequestedRows(),
                    audit.getAffectedRows(),
                    audit.getExecutedStatements(),
                    audit.getOperator(),
                    toJson(audit.getExtra()),
                    audit.getOperatedAt() == null ? null : Timestamp.valueOf(audit.getOperatedAt()));
        } catch (Exception e) {
            // 审计失败不能影响已经成功的业务更新
            log.error("Failed to record batch update audit for {}", audit.getEntityName(), e);
        }
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            // 序列化失败时返回 null，不要写入非 JSON 文本污染该列
            log.warn("Failed to serialize batch update audit field, value dropped", e);
            return null;
        }
    }
}
