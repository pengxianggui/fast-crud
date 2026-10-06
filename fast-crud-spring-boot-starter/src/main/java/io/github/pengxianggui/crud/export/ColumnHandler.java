package io.github.pengxianggui.crud.export;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import io.github.pengxianggui.crud.FastCrudProperty;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
        this.props = columnProps != null ? columnProps : new HashMap<>();

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
     * 导入时, 将excel单元格的文本值转换为业务字段值。
     * <p>
     * 默认实现: 去除首尾空白后原样返回字符串; 空白单元格返回null, 交由后续校验(@NotBlank等)处理。
     * 子类可覆盖以实现类型转换(数字、日期、下拉选项等)
     *
     * @param text      单元格文本值(可能为null)
     * @param fieldType 目标DTO中对应字段的java类型(可能为null)
     * @return 转换后的字段值
     */
    public Object parseImportValue(String text, Class<?> fieldType) {
        return StrUtil.trimToNull(text);
    }

    /**
     * 获取该列的可选项文案(用于导出时生成下拉校验)。默认无选项。
     *
     * @return 选项文案列表, 顺序即下拉展示顺序
     */
    public List<String> getOptionLabels() {
        return Collections.emptyList();
    }

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
