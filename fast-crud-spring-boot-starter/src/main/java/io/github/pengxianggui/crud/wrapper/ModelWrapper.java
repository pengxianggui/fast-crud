package io.github.pengxianggui.crud.wrapper;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Data
public class ModelWrapper<T> {

    @JsonUnwrapped
    @Valid
    private T model;

    @JsonIgnore
    public void setModel(T model) {
        this.model = model;
    }

    public ModelWrapper() {
    }

    public ModelWrapper(@NotNull T model) {
        this.model = model;
    }

    /**
     * 获取类主键字段名称
     *
     * @return
     */
    public String getPkName() {
        if (model == null) {
            return null;
        }
        try {
            TableInfo tableInfo = TableInfoHelper.getTableInfo(model.getClass());
            if (tableInfo != null && StrUtil.isNotBlank(tableInfo.getKeyProperty())) {
                return tableInfo.getKeyProperty();
            }
            return null;
        } catch (Exception e) {
            throw new IllegalArgumentException("无法获取主键");
        }
    }

    /**
     * 获取主键值
     *
     * @return
     */
    public Serializable getPkVal() {
        return (Serializable) ReflectUtil.getFieldValue(model, getPkName());
    }

    /**
     * 获取数据库字段名称
     *
     * @param fieldName model类字段名
     * @return
     */
    public String getDbFieldName(String fieldName) {
        Assert.notNull(model, "model值为null, 无法获取属性(%s)映射的数据库字段名", fieldName);
        TableInfo tableInfo = TableInfoHelper.getTableInfo(model.getClass());
        if (tableInfo == null) {
            throw new IllegalStateException("无法获取实体类的TableInfo");
        }
        // 通过字段名获取映射的数据库字段名
        return tableInfo.getFieldList().stream()
                .filter(meta -> Objects.equals(meta.getProperty(), fieldName))
                .map(meta -> meta.getColumn())
                .findFirst()
                .orElse(null);
    }
}
