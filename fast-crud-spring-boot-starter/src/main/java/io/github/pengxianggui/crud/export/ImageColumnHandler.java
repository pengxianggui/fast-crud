package io.github.pengxianggui.crud.export;

import cn.hutool.core.io.resource.FileResource;
import cn.hutool.core.io.resource.UrlResource;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.ReUtil;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import io.github.pengxianggui.crud.Constant;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author pengxg
 * @date 2025/5/3 10:49
 */
public class ImageColumnHandler extends ColumnHandler {
    public ImageColumnHandler(String component, Map<String, Object> columnConfig) {
        super(component, columnConfig);
    }

    @Override
    public void handleHead(WriteSheetHolder writeSheetHolder, CellStyle cellStyle, int columnIndex) {
        super.handleHead(writeSheetHolder, cellStyle, columnIndex);
        // TODO 设置图片列样式
    }

    @Override
    public void handleData(CellWriteHandlerContext context, Object value) {
        Cell cell = context.getCell();
        String strVal = String.valueOf(value);
        try {
            if (value != null) {
                // TODO 多图片问题
                byte[] imageBytes = getImageBytes(strVal);
                if (imageBytes != null) {
                    Drawing<?> drawing = cell.getSheet().createDrawingPatriarch();
                    ClientAnchor anchor = new XSSFClientAnchor();
                    anchor.setCol1(cell.getColumnIndex());
                    anchor.setRow1(cell.getRowIndex());
                    anchor.setCol2(cell.getColumnIndex() + 1);
                    anchor.setRow2(cell.getRowIndex() + 1);
                    drawing.createPicture(anchor,
                            cell.getSheet().getWorkbook().addPicture(imageBytes, HSSFWorkbook.PICTURE_TYPE_JPEG));
                }
            }
        } catch (Exception e) {
            cell.setCellValue(strVal);
        }
    }

    private byte[] getImageBytes(String url) throws MalformedURLException {
        if (ReUtil.isMatch(Constant.HTTP_REGEX, url)) {
            return new UrlResource(new URL(url)).readBytes();
        }
        Map<CharSequence, CharSequence> queryMap = UrlQuery.of(url, Charset.defaultCharset()).getQueryMap();
        if (queryMap.containsKey("path")) {
            return new FileResource(String.valueOf(queryMap.get("path"))).readBytes();
        }
        return null;
    }
}
