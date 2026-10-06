package io.github.pengxianggui.crud.importer;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pengxianggui.crud.export.ColumnHandler;
import io.github.pengxianggui.crud.export.ExcelExportManager;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel导入管理器。与{@link ExcelExportManager}复用同一套列处理器({@link ColumnHandler})与列元数据。
 * <p>
 * 读取时以导出模板的表头({@code label})匹配列, 未知表头忽略, 空行跳过。
 *
 * @author pengxg
 */
@Slf4j
public class ExcelImportManager {
    private final ObjectMapper objectMapper;

    public ExcelImportManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 解析excel为DTO列表
     *
     * @param inputStream 输入流
     * @param columns     列配置(与导出列元数据同构), 其中importable=false的列不参与导入
     * @param dtoClazz    目标DTO类型
     * @param <DTO>       目标DTO类型
     * @return 解析结果(含成功行与错误明细)
     */
    public <DTO> ImportParseResult<DTO> parse(InputStream inputStream, List<Map<String, Object>> columns,
                                              Class<DTO> dtoClazz) {
        List<Map<String, Object>> importColumns = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(columns)) {
            for (Map<String, Object> column : columns) {
                if (Boolean.FALSE.equals(column.get("importable"))) {
                    continue;
                }
                if (StrUtil.isBlank((String) column.get("col")) || StrUtil.isBlank((String) column.get("label"))) {
                    continue;
                }
                importColumns.add(column);
            }
        }

        ImportReadListener listener = new ImportReadListener();
        EasyExcel.read(inputStream, listener).headRowNumber(1).sheet().doRead();

        // 表头 -> 列元数据(按label匹配)
        Map<Integer, ImportColumn> headIndexMapping = new LinkedHashMap<>();
        for (Map.Entry<Integer, String> entry : listener.getHeadMap().entrySet()) {
            String label = StrUtil.trim(entry.getValue());
            if (StrUtil.isBlank(label)) {
                continue;
            }
            Map<String, Object> matched = importColumns.stream()
                    .filter(column -> label.equals(StrUtil.trim((String) column.get("label"))))
                    .findFirst()
                    .orElse(null);
            if (matched == null) {
                log.debug("Fast crud import: unknown column head [{}] ignored!", label);
                continue;
            }
            String col = (String) matched.get("col");
            Field field = findField(dtoClazz, col);
            if (field == null) {
                log.debug("Fast crud import: field [{}] not found in {}, ignored!", col, dtoClazz.getName());
                continue;
            }
            headIndexMapping.put(entry.getKey(),
                    new ImportColumn(col, label, field.getType(), ExcelExportManager.getColumnHandler(matched)));
        }

        List<ImportRow<DTO>> rows = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();
        for (RowData rowData : listener.getRows()) {
            parseRow(rowData, headIndexMapping, dtoClazz, rows, errors);
        }
        return new ImportParseResult<>(rows, errors);
    }

    private <DTO> void parseRow(RowData rowData, Map<Integer, ImportColumn> headIndexMapping,
                                Class<DTO> dtoClazz, List<ImportRow<DTO>> rows, List<ImportError> errors) {
        Map<String, Object> modelMap = new LinkedHashMap<>();
        List<ImportError> rowErrors = new ArrayList<>();
        boolean hasValue = false;
        for (Map.Entry<Integer, ImportColumn> entry : headIndexMapping.entrySet()) {
            ImportColumn column = entry.getValue();
            String text = rowData.getCells().get(entry.getKey());
            if (StrUtil.isNotBlank(text)) {
                hasValue = true;
            }
            try {
                modelMap.put(column.getCol(), column.getHandler().parseImportValue(text, column.getFieldType()));
            } catch (Exception e) {
                rowErrors.add(new ImportError(rowData.getRow(), column.getCol(), column.getLabel(), e.getMessage()));
            }
        }
        if (!hasValue) { // 空行跳过
            return;
        }
        if (!rowErrors.isEmpty()) {
            errors.addAll(rowErrors);
            return;
        }
        try {
            DTO model = objectMapper.convertValue(modelMap, dtoClazz);
            rows.add(new ImportRow<>(rowData.getRow(), model));
        } catch (Exception e) {
            errors.add(new ImportError(rowData.getRow(), null, null, "Failed to convert row data: " + rootMessage(e)));
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) {
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                // 继续向父类查找
            }
        }
        return null;
    }

    private static String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return StrUtil.blankToDefault(root.getMessage(), root.getClass().getSimpleName());
    }

    /**
     * 一列的解析配置
     */
    private static class ImportColumn {
        private final String col;
        private final String label;
        private final Class<?> fieldType;
        private final ColumnHandler handler;

        ImportColumn(String col, String label, Class<?> fieldType, ColumnHandler handler) {
            this.col = col;
            this.label = label;
            this.fieldType = fieldType;
            this.handler = handler;
        }

        String getCol() {
            return col;
        }

        String getLabel() {
            return label;
        }

        Class<?> getFieldType() {
            return fieldType;
        }

        ColumnHandler getHandler() {
            return handler;
        }
    }

    /**
     * 一行原始数据(excel行号 + 单元格文本)
     */
    private static class RowData {
        private final int row;
        private final Map<Integer, String> cells;

        RowData(int row, Map<Integer, String> cells) {
            this.row = row;
            this.cells = cells;
        }

        int getRow() {
            return row;
        }

        Map<Integer, String> getCells() {
            return cells;
        }
    }

    /**
     * easyexcel读取监听器: 收集表头与数据行
     */
    private static class ImportReadListener extends AnalysisEventListener<Map<Integer, String>> {
        private final Map<Integer, String> headMap = new LinkedHashMap<>();
        private final List<RowData> rows = new ArrayList<>();

        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
            this.headMap.putAll(headMap);
        }

        @Override
        public void invoke(Map<Integer, String> data, AnalysisContext context) {
            Integer rowIndex = context.readRowHolder().getRowIndex();
            rows.add(new RowData(rowIndex == null ? 0 : rowIndex + 1, data));
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
        }

        Map<Integer, String> getHeadMap() {
            return headMap;
        }

        List<RowData> getRows() {
            return rows;
        }
    }
}
