package io.github.pengxianggui.crud.wrapper;

import io.github.pengxianggui.crud.valid.CrudUpdate;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class UpdateModelWrapper<T> extends ModelWrapper<T> {

    @ApiModelProperty(value = "是否更新空值（null、空字符串）", example = "true")
    private boolean updateNull = true;

    public UpdateModelWrapper() {
    }

    public UpdateModelWrapper(@NotNull T model, boolean updateNull, String... validCols) {
        super(model, validCols);
        this.updateNull = updateNull;
    }

    public void validate(Validator validator) throws BindException {
        Set<ConstraintViolation<UpdateModelWrapper<T>>> validates;
        validates = validator.validate(this, CrudUpdate.class);
        if (validates != null && !validates.isEmpty()) {
            BindException be = new BindException(this, "modelWrapper");
            for (ConstraintViolation<UpdateModelWrapper<T>> validate : validates) {
                String[] paths = validate.getPropertyPath().toString().split("\\.");

                Stream<String> stream = Arrays.stream(paths);
                if (paths.length > 1) {
                    stream = stream.skip(1);
                }
                List<String> collect = stream.collect(Collectors.toList());
                String join = Strings.join(collect, '.');
                be.addError(new FieldError("", join, validate.getMessage()));
            }
            throw be;
        }
    }
}
