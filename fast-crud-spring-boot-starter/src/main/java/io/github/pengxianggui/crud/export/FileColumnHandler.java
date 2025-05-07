package io.github.pengxianggui.crud.export;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import io.github.pengxianggui.crud.Constant;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author pengxg
 * @date 2025/5/7 11:23
 */
public class FileColumnHandler extends ColumnHandler {
    private ValType valType;

    public FileColumnHandler(String component, Map<String, Object> columnConfig) {
        super(component, columnConfig);
        try {
            this.valType = ValType.valueOf((String) columnConfig.getOrDefault("valType", "link"));
        } catch (IllegalArgumentException e) {
            this.valType = ValType.link;
        }
    }

    @Override
    public void handleData(CellWriteHandlerContext context, Object value) {
        WriteSheetHolder writeSheetHolder = context.getWriteSheetHolder();
        CreationHelper createHelper = writeSheetHolder.getSheet().getWorkbook().getCreationHelper();
        if (value == null) {
            return;
        }
        Cell cell = context.getCell();
        String strVal = String.valueOf(value);
        try {
            if (this.valType == ValType.link) {
                String url = getFileUrl(strVal);
                if (StrUtil.isBlank(url)) {
                    cell.setCellValue(strVal);
                    return;
                }
                Hyperlink hyperlink = createHelper.createHyperlink(HyperlinkType.URL);
                hyperlink.setAddress(url);
                String fileName = getFileName(strVal);
                hyperlink.setLabel(fileName);
                cell.setHyperlink(hyperlink);
                CellStyle style = writeSheetHolder.getSheet().getWorkbook().createCellStyle();
                Font font = writeSheetHolder.getSheet().getWorkbook().createFont();
                font.setUnderline(Font.U_SINGLE);
                font.setColor(IndexedColors.BLUE.getIndex());
                style.setFont(font);
                cell.setCellStyle(style);
                cell.setCellValue(fileName);
            }
        } catch (Exception e) {
            cell.setCellValue(strVal);
        }
    }

    private String getFileName(String url) {
        if (ReUtil.isMatch(Constant.HTTP_REGEX, url)) {
            return FileUtil.getName(url);
        }
        Map<CharSequence, CharSequence> queryMap = UrlQuery.of(url, Charset.defaultCharset()).getQueryMap();
        if (queryMap.containsKey("path")) {
            return FileUtil.getName(String.valueOf(queryMap.get("path")));
        }
        return null;
    }

    private String getFileUrl(String url) {
        if (ReUtil.isMatch(Constant.HTTP_REGEX, url)) {
            return url;
        }
        Map<CharSequence, CharSequence> queryMap = UrlQuery.of(url, Charset.defaultCharset()).getQueryMap();
        if (queryMap.containsKey("path")) {
            return this.host + StrUtil.addPrefixIfNot(url, File.separator);
        }
        return null;
    }

    enum ValType {
        link
    }
}
