package io.github.pengxianggui.crud.export;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import io.github.pengxianggui.crud.importer.ImportCellParseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author pengxg
 * @date 2025/5/3 10:57
 */
public class SelectColumnHandler extends ColumnHandler {
    private Map<Object, String> options; // key为value，value为label

    public SelectColumnHandler(String component, Map<String, Object> columnConfig) {
        super(component, columnConfig);
        this.options = resolveOptions();
    }

    /**
     * 解析"取值 -> 展示文案"的映射关系。
     * <p>
     * switch列同样复用本处理器, 取activeValue/inactiveValue(默认true/false)与activeText/inactiveText(默认是/否),
     * 这样无论是导出还是导入模板, 开关值都能正确转义(如true <-> Y)
     */
    protected Map<Object, String> resolveOptions() {
        Map<Object, String> mapping = new LinkedHashMap<>();
        if (isSwitch()) {
            putOption(mapping, props.get("activeValue"), Boolean.TRUE, (String) props.get("activeText"), "是");
            putOption(mapping, props.get("inactiveValue"), Boolean.FALSE, (String) props.get("inactiveText"), "否");
            return mapping;
        }
        Object options = this.props.get("options");
        if (options instanceof List) {
            String valKey = StrUtil.blankToDefault((String) this.props.get("valKey"), "value");
            String labelKey = StrUtil.blankToDefault((String) this.props.get("labelKey"), "label");
            ((List<?>) options).stream().filter(item -> item instanceof Map)
                    .map(item -> (Map<?, ?>) item)
                    .forEach(item -> putOption(mapping, item.get(valKey), null, (String) item.get(labelKey), null));
        }
        return mapping;
    }

    private boolean isSwitch() {
        return "fast-table-column-switch".equals(this.component) || "FastTableColumnSwitch".equals(this.component);
    }

    private static void putOption(Map<Object, String> mapping, Object value, Object defaultVal,
                                  String label, String defaultLabel) {
        Object optionValue = value != null ? value : defaultVal;
        if (optionValue == null) {
            return;
        }
        String optionLabel = StrUtil.blankToDefault(label, defaultLabel);
        if (StrUtil.isBlank(optionLabel)) {
            return;
        }
        mapping.put(optionValue, optionLabel);
    }

    @Override
    public void handleHead(WriteSheetHolder writeSheetHolder, CellStyle cellStyle, int columnIndex) {
        Sheet sheet = writeSheetHolder.getSheet();
        DataValidationHelper helper = sheet.getDataValidationHelper();
        CellRangeAddressList rangeList = new CellRangeAddressList(1, 65535, columnIndex, columnIndex);

        if (!CollectionUtil.isEmpty(this.options)) {
            String[] labels = this.options.values().toArray(new String[this.options.size()]);
            String joinedLabels = String.join(",", labels);
            // 下拉列表逗号连接后的长度不能超过255个字符，否则会报错，如果超过则降级不做成下拉列表
            if (joinedLabels.length() <= 255) {
                DataValidationConstraint constraint = helper.createExplicitListConstraint(labels);
                DataValidation validation = helper.createValidation(constraint, rangeList);
                validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
                validation.createErrorBox("ERROR", "Please choose value of options");
                sheet.addValidationData(validation);
            }
        }
    }

    @Override
    public void handleData(CellWriteHandlerContext context, Object value) {
        // TODO 处理多选
        String label = labelOf(value);
        Cell cell = context.getCell();
        if (StrUtil.isBlank(label)) {
            cell.setCellValue(value != null ? String.valueOf(value) : "");
        } else {
            cell.setCellValue(label);
        }
    }

    /**
     * 找到值对应的展示文案。兼容value类型与字段值类型不一致的场景(如activeValue配置为"1", 而字段值为1)
     *
     * @param value 字段值
     * @return 展示文案; 未匹配到返回null
     */
    protected String labelOf(Object value) {
        if (value == null || options.isEmpty()) {
            return null;
        }
        String label = options.get(value);
        if (label != null) {
            return label;
        }
        String strValue = String.valueOf(value).trim();
        for (Map.Entry<Object, String> entry : options.entrySet()) {
            if (strValue.equals(String.valueOf(entry.getKey()).trim())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public Object parseImportValue(String text, Class<?> fieldType) {
        String value = StrUtil.trimToNull(text);
        if (value == null) {
            return null;
        }
        // 选项为空(如动态选项)时无法做映射, 原样返回交由业务钩子处理
        if (CollectionUtil.isEmpty(this.options)) {
            return value;
        }
        // 优先按value精确匹配(允许用户直接填value)
        for (Map.Entry<Object, String> entry : this.options.entrySet()) {
            if (Objects.equals(value, String.valueOf(entry.getKey()).trim())) {
                return entry.getKey();
            }
        }
        // 再按label反查value
        for (Map.Entry<Object, String> entry : this.options.entrySet()) {
            if (Objects.equals(value, String.valueOf(entry.getValue()).trim())) {
                return entry.getKey();
            }
        }
        String labels = this.options.values().stream().filter(Objects::nonNull).collect(Collectors.joining("/"));
        throw new ImportCellParseException("Invalid option value, expected one of [" + labels + "], actual value: " + value);
    }
}
