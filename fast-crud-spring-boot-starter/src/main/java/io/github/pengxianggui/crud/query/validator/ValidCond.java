package io.github.pengxianggui.crud.query.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {CondValidator.class})
public @interface ValidCond {
    String message() default "Condition field and nested condition cannot both be empty";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
