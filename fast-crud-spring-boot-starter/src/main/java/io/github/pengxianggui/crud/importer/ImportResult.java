package io.github.pengxianggui.crud.importer;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 导入结果。导入接口始终以HTTP 200返回此结构, 由success字段标识导入是否成功。
 *
 * @author pengxg
 */
@Data
public class ImportResult {
    /**
     * 整体是否成功。任一环节失败(false)时, 本次导入全部回滚, 不会有任何数据写入
     */
    private boolean success;
    /**
     * 本次解析出的总行数
     */
    private int total;
    /**
     * 新增成功的条数
     */
    private int inserted;
    /**
     * 更新成功的条数
     */
    private int updated;
    /**
     * 失败明细。success为false时非空
     */
    private List<ImportError> errors = new ArrayList<>();

    public static ImportResult success(int total, int inserted, int updated) {
        ImportResult result = new ImportResult();
        result.setSuccess(true);
        result.setTotal(total);
        result.setInserted(inserted);
        result.setUpdated(updated);
        result.setErrors(Collections.emptyList());
        return result;
    }

    public static ImportResult fail(int total, List<ImportError> errors) {
        ImportResult result = new ImportResult();
        result.setSuccess(false);
        result.setTotal(total);
        result.setErrors(errors == null ? new ArrayList<>() : errors);
        return result;
    }

    public static ImportResult fail(String message) {
        return fail(0, Collections.singletonList(ImportError.of(message)));
    }
}
