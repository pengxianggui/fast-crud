package io.github.pengxianggui.crud.util;

import org.apache.logging.log4j.util.Strings;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author pengxg
 * @date 2024-12-02 17:25
 */
public class ValidUtil {

    public static <T> boolean valid(Validator validator, T model, Class<?> group) throws BindException {
        Set<ConstraintViolation<T>> validates;
        validates = validator.validate(model, group);
        if (validates != null && !validates.isEmpty()) {
            BindException be = new BindException(model, "model");
            for (ConstraintViolation<T> validate : validates) {
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
        return true;
    }
}
