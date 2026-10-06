package io.github.pengxianggui.crud.export;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import io.github.pengxianggui.crud.importer.ImportCellParseException;
import org.apache.poi.ss.usermodel.Cell;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * @author pengxg
 * @date 2025/5/3 10:56
 */
public class NumberColumnHandler extends ColumnHandler {

    public NumberColumnHandler(String component, Map<String, Object> columnConfig) {
        super(component, columnConfig);
    }

    @Override
    public void handleData(CellWriteHandlerContext context, Object value) {
        if (value != null) {
            Cell cell = context.getCell();
            try {
                cell.setCellValue(Double.parseDouble(value.toString()));
            } catch (Exception e) {
                cell.setCellValue(value.toString());
            }
        }
    }

    @Override
    public Object parseImportValue(String text, Class<?> fieldType) {
        String value = StrUtil.trimToNull(text);
        if (value == null) {
            return null;
        }
        // 兼容千分位分隔符
        String normalized = value.replace(",", "");
        try {
            BigDecimal decimal = new BigDecimal(normalized);
            if (Integer.class.equals(fieldType) || int.class.equals(fieldType)) {
                return decimal.intValueExact();
            }
            if (Long.class.equals(fieldType) || long.class.equals(fieldType)) {
                return decimal.longValueExact();
            }
            if (Short.class.equals(fieldType) || short.class.equals(fieldType)) {
                return decimal.shortValueExact();
            }
            if (Byte.class.equals(fieldType) || byte.class.equals(fieldType)) {
                return decimal.byteValueExact();
            }
            if (Float.class.equals(fieldType) || float.class.equals(fieldType)) {
                return decimal.floatValue();
            }
            if (Double.class.equals(fieldType) || double.class.equals(fieldType)) {
                return decimal.doubleValue();
            }
            if (BigInteger.class.equals(fieldType)) {
                return decimal.toBigIntegerExact();
            }
            return decimal;
        } catch (Exception e) {
            throw new ImportCellParseException("Invalid number: " + value);
        }
    }
}
