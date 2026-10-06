package io.github.pengxianggui.crud.importer;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 导入异常。抛出后当前事务将回滚(整批回滚), 由controller捕获并转换为{@link ImportResult}。
 *
 * @author pengxg
 */
@Getter
public class ImportException extends RuntimeException {
    private final List<ImportError> errors;

    public ImportException(List<ImportError> errors) {
        super(buildMessage(errors));
        this.errors = errors == null ? Collections.emptyList() : errors;
    }

    public ImportException(String message) {
        super(message);
        this.errors = Collections.singletonList(ImportError.of(message));
    }

    private static String buildMessage(List<ImportError> errors) {
        if (errors == null || errors.isEmpty()) {
            return "Import failed";
        }
        return errors.stream()
                .limit(5)
                .map(e -> (e.getRow() > 0 ? "Row " + e.getRow() + ": " : "") + e.getMessage())
                .collect(Collectors.joining("; "));
    }
}
