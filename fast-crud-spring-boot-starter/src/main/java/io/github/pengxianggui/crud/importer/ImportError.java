package io.github.pengxianggui.crud.importer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导入过程中的一条错误明细。
 *
 * @author pengxg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportError {
    /**
     * excel行号(从1开始, 含表头行)。为0或负数时表示与具体行无关的错误
     */
    private int row;
    /**
     * 字段名(java属性名)
     */
    private String col;
    /**
     * 字段展示名(表头名)
     */
    private String label;
    /**
     * 错误原因
     */
    private String message;

    public static ImportError of(String message) {
        return new ImportError(0, null, null, message);
    }
}
