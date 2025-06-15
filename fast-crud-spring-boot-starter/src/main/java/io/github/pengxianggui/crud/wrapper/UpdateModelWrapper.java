package io.github.pengxianggui.crud.wrapper;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdateModelWrapper<T> extends ModelWrapper<T> {
    @ApiModelProperty(value = "是否更新空值(null、空字符串)。如果此值为null则表示不干预，全权由后端决定; 若此值为true, 则表示要更新null值(但优先级仍低于后端设置); 若此值为false, 则表示不更新null值(但优先级仍低于后端设置)。", example = "true")
    private Boolean _updateNull = null;

    public UpdateModelWrapper() {
    }

    public UpdateModelWrapper(@NotNull T model) {
        this(model, false);
    }

    public UpdateModelWrapper(@NotNull T model, boolean _updateNull) {
        super(model);
        this._updateNull = _updateNull;
    }

}
