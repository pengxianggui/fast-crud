package io.github.pengxianggui.crud.query.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {QueryValidator.class})
public @interface ValidQuery {
    String message() default "The col field in orders must be in cols";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
