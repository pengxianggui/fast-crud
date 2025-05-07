package io.github.pengxianggui.crud.export;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import io.github.pengxianggui.crud.FastCrudProperty;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

import java.util.Map;

/**
 * 列处理器接口
 *
 * @author pengxg
 * @date 2025/5/3 10:33
 */
public abstract class ColumnHandler {
    protected String component;
    protected Map<String, Object> columnConfig;
    protected Map<String, Object> props;
    /**
     * 本服务地址(fast-crud.host)
     */
    protected String host;

    public ColumnHandler(String component, Map<String, Object> columnConfig) {
        this.component = component;
        this.columnConfig = columnConfig;
        Map<String, Object> columnProps = (Map<String, Object>) columnConfig.get("props");
        this.props = columnProps;

        FastCrudProperty property = SpringUtil.getBean(FastCrudProperty.class);
        this.host = property.getHost();
    }

    /**
     * 处理列头
     *
     * @param writeSheetHolder
     * @param cellStyle
     * @param columnIndex      列号
     */
    public void handleHead(WriteSheetHolder writeSheetHolder, CellStyle cellStyle, int columnIndex) {
        Font font = writeSheetHolder.getSheet().getWorkbook().createFont();
//        font.setFontName("Microsoft YaHei");
        font.setBold(true);
        cellStyle.setFont(font);
    }

    /**
     * 处理单元格数据
     *
     * @param context
     * @param value   单元格数据值
     */
    public abstract void handleData(CellWriteHandlerContext context, Object value);

    /**
     * 获取列宽
     *
     * @return
     */
    public int getColumnWidth() {
        try {
            String minWidth = StrUtil.blankToDefault((String) props.get("minWidth"), "90px");
            String width = (String) props.get("width");
            String resultWidth = StrUtil.blankToDefault(width, minWidth);
            return Integer.parseInt(resultWidth.replace("px", "")) / 7; // 一个excel字符宽度约7px
        } catch (Exception e) {
            return 90;
        }
    }
}
