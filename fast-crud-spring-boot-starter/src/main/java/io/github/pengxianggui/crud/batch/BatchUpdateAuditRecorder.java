package io.github.pengxianggui.crud.batch;

/**
 * 批量修改审计记录的输出端口。
 * 默认实现 {@link JdbcBatchUpdateAuditRecorder} 写入 fc_batch_update_log 表，
 * 若已有统一的审计平台，声明一个本接口的 Bean 即可覆盖。
 */
public interface BatchUpdateAuditRecorder {

    /**
     * 记录一次批量修改。
     * 实现需自行吞掉异常，审计失败不应导致已成功的业务更新回滚。
     */
    void record(BatchUpdateAudit audit);
}
