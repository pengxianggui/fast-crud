package io.github.pengxianggui.crud.batch;

import lombok.extern.slf4j.Slf4j;

/**
 * 无数据源时的兜底实现，把审计内容以 INFO 日志输出，保证批量修改功能可用。
 */
@Slf4j
public class LoggingBatchUpdateAuditRecorder implements BatchUpdateAuditRecorder {

    @Override
    public void record(BatchUpdateAudit audit) {
        if (audit == null) {
            return;
        }
        log.info("[batch-update-audit] entity={} ids={} fields={} newValues={} requested={} affected={} statements={} operator={} at={}",
                audit.getEntityName(), audit.getIds(), audit.getFields(), audit.getNewValues(),
                audit.getRequestedRows(), audit.getAffectedRows(), audit.getExecutedStatements(),
                audit.getOperator(), audit.getOperatedAt());
    }
}
