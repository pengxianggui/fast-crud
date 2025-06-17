package io.github.pengxianggui.crud.dao.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pengxianggui.crud.file.FileItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.*;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * @author pengxg
 * @date 2025/6/16 17:33
 */
@Slf4j
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(List.class)
public class FileItemTypeHandler implements TypeHandler<List<FileItem>> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void setParameter(PreparedStatement ps, int i, List<FileItem> parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.isEmpty()) {
            ps.setString(i, "[]");
        } else {
            ps.setString(i, toStr(parameter));
        }
    }

    private String toStr(List<FileItem> parameter) {
        if (parameter == null) {
            return null;
        }
        String val = null;
        try {
            val = mapper.writeValueAsString(parameter);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return val;
    }

    @Override
    public List<FileItem> getResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public List<FileItem> getResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public List<FileItem> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    private List<FileItem> parseJson(String json) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, FileItem.class);
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new SQLException("Failed to parse JSON to List<FileItem>: " + json, e);
        }
    }
}
