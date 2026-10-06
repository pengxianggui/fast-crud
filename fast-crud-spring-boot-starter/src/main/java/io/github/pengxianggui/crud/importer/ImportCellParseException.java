package io.github.pengxianggui.crud.importer;

/**
 * 单元格解析异常。解析某个单元格时抛出, 由{@link ExcelImportManager}捕获并转为行级错误。
 *
 * @author pengxg
 */
public class ImportCellParseException extends RuntimeException {
    public ImportCellParseException(String message) {
        super(message);
    }
}
