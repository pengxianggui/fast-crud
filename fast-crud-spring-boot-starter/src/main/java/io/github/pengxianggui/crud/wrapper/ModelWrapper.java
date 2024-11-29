package io.github.pengxianggui.crud.wrapper;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Data
public class ModelWrapper<T> {

    // TODO 确定必要性
    @ApiModelProperty("有效字段")
    @JsonIgnore
    private Set<String> validCols = new HashSet<>();

    @JsonAnySetter
    private void setValue(String key, Object value) {
        validCols.add(key);
    }

    @JsonUnwrapped
    @Valid
    private T model;

    @JsonIgnore
    public void setModel(T model) {
        this.model = model;
    }

    public ModelWrapper() {
    }

    public ModelWrapper(@NotNull T model, String... validCols) {
        if (validCols != null) {
            this.validCols.addAll(Arrays.asList(validCols));
        }
        this.model = model;
    }

    /**
     * 获取主键名称
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
}
