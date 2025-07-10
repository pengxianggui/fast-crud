package io.github.pengxianggui.crud.export;


import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

/**
 * 多文件只写第一个
 *
 * @author pengxg
 * @date 2025/7/10 22:12
 */
@Slf4j
public class ArrayListConverter implements Converter<ArrayList> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Class<?> supportJavaTypeKey() {
        return ArrayList.class; // 要升级easyexcel 到5.0+，支持泛型
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public WriteCellData<?> convertToExcelData(ArrayList value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        if (CollectionUtil.isEmpty(value)) {
            return new WriteCellData<>("[]");
        }
        try {
            return new WriteCellData<>(objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return new WriteCellData<>("[处理失败]");
        }
    }
}
