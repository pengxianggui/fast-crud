package io.github.pengxianggui.crud.export;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author pengxg
 * @date 2025/5/3 10:57
 */
public class SelectColumnHandler extends ColumnHandler {
    private Map<Object, String> options; // key为value，value为label

    public SelectColumnHandler(String component, Map<String, Object> columnConfig) {
        super(component, columnConfig);
        this.options = new HashMap<>();
        if ("fast-table-column-switch".equals(component)) {
            this.options.put(this.props.get("activeValue"), (String) this.props.get("activeText"));
            this.options.put(this.props.get("inactiveValue"), (String) this.props.get("inactiveText"));
        } else {
            Object options = this.props.get("options");
            if (options instanceof List) {
                String valKey = StrUtil.blankToDefault((String) this.props.get("valKey"), "value");
                String labelKey = StrUtil.blankToDefault((String) this.props.get("labelKey"), "label");
                this.options = ((List<?>) options).stream().filter(item -> item instanceof Map)
                        .map(item -> (Map<?, ?>) item)
                        .collect(Collectors.toMap(item -> item.get(valKey), item -> (String) item.get(labelKey)));
            }
        }

    }

    @Override
    public void handleHead(WriteSheetHolder writeSheetHolder, CellStyle cellStyle, int columnIndex) {
        Sheet sheet = writeSheetHolder.getSheet();
        DataValidationHelper helper = sheet.getDataValidationHelper();
        CellRangeAddressList rangeList = new CellRangeAddressList(1, 65535, columnIndex, columnIndex);

        if (!CollectionUtil.isEmpty(this.options)) {
            String[] labels = this.options.values().toArray(new String[this.options.size()]);
            DataValidationConstraint constraint = helper.createExplicitListConstraint(labels);
            DataValidation validation = helper.createValidation(constraint, rangeList);
            // 禁止输入非下拉选项
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.createErrorBox("错误", "请选择下拉选项中的值");
            sheet.addValidationData(validation);
        }
    }

    @Override
    public void handleData(CellWriteHandlerContext context, Object value) {
        // TODO 处理多选
        String label = options.get(value);
        Cell cell = context.getCell();
        if (StrUtil.isBlank(label)) {
            cell.setCellValue(value != null ? String.valueOf(value) : "");
        } else {
            cell.setCellValue(label);
        }
    }
}
