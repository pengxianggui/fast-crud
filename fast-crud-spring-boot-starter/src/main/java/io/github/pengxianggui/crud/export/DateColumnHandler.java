package io.github.pengxianggui.crud.export;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;

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
}
