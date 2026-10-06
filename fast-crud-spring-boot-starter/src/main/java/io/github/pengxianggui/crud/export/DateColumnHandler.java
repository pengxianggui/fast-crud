package io.github.pengxianggui.crud.export;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import io.github.pengxianggui.crud.importer.ImportCellParseException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author pengxg
 * @date 2025/5/3 10:59
 */
public class DateColumnHandler extends ColumnHandler {

    public DateColumnHandler(String component, Map<String, Object> columnConfig) {
        super(component, columnConfig);
    }

    @Override
    public void handleHead(WriteSheetHolder writeSheetHolder, CellStyle cellStyle, int columnIndex) {
        super.handleHead(writeSheetHolder, cellStyle, columnIndex);
        // 可以设置日期列样式
        CreationHelper createHelper = writeSheetHolder.getSheet().getWorkbook().getCreationHelper();
        String format = StrUtil.blankToDefault((String) props.get("value-format"), "yyyy-MM-dd HH:mm:ss");
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat(format));
    }

    @Override
    public void handleData(CellWriteHandlerContext context, Object value) {
        if (value != null) {
            context.getCell().setCellValue(value.toString());
        }
    }

    @Override
    public Object parseImportValue(String text, Class<?> fieldType) {
        String value = StrUtil.trimToNull(text);
        if (value == null) {
            return null;
        }
        // 时间戳字段(如Long/Integer: 毫秒值)
        if (fieldType != null && (Number.class.isAssignableFrom(fieldType)
                || long.class.equals(fieldType) || int.class.equals(fieldType))) {
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException e) {
                throw new ImportCellParseException("Invalid timestamp: " + value);
            }
        }

        String configuredFormat = normalizeDatePattern(
                StrUtil.blankToDefault((String) props.get("value-format"), "yyyy-MM-dd HH:mm:ss"));
        for (String pattern : candidateFormats(configuredFormat)) {
            Object parsed = tryParseDate(value, pattern, fieldType);
            if (parsed != null) {
                return parsed;
            }
        }
        throw new ImportCellParseException("Invalid date, expected format: " + configuredFormat + ", actual value: " + value);
    }

    private Object tryParseDate(String value, String pattern, Class<?> fieldType) {
        boolean datePattern = pattern.contains("y");
        boolean timePattern = pattern.contains("H") || pattern.contains("h") || pattern.contains("m");
        try {
            if (LocalDateTime.class.equals(fieldType)) {
                if (datePattern && timePattern) {
                    return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(pattern));
                }
                if (datePattern) {
                    return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern)).atStartOfDay();
                }
                return null;
            }
            if (LocalDate.class.equals(fieldType)) {
                return datePattern ? LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern)) : null;
            }
            if (LocalTime.class.equals(fieldType)) {
                return LocalTime.parse(value, DateTimeFormatter.ofPattern(pattern));
            }
            if (Instant.class.equals(fieldType)) {
                return new SimpleDateFormat(pattern).parse(value).toInstant();
            }
            if (fieldType != null && Date.class.isAssignableFrom(fieldType)) {
                return new SimpleDateFormat(pattern).parse(value);
            }
        } catch (Exception ignored) {
            // 尝试下一个格式
        }
        return null;
    }

    private static List<String> candidateFormats(String configuredFormat) {
        List<String> formats = new ArrayList<>();
        formats.add(configuredFormat);
        for (String format : new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd",
                "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd", "yyyy年MM月dd日", "HH:mm:ss"}) {
            if (!formats.contains(format)) {
                formats.add(format);
            }
        }
        return formats;
    }

    /**
     * 将前端常见的dayjs风格格式(如YYYY-MM-DD HH:mm:ss)转换为java格式(yyyy-MM-dd HH:mm:ss)
     */
    private static String normalizeDatePattern(String pattern) {
        if (StrUtil.isBlank(pattern)) {
            return pattern;
        }
        return pattern.replace("YYYY", "yyyy")
                .replace("YY", "yy")
                .replace("DD", "dd")
                .replace("D", "d");
    }
}
