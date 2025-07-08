package io.github.pengxianggui.crud.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class ModelWrapper<T> {

    @NotNull
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

}
