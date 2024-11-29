package io.github.pengxianggui.crud.query.validator;

import cn.hutool.core.util.StrUtil;
import io.github.pengxianggui.crud.query.Cond;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CondValidator implements ConstraintValidator<ValidCond, Cond> {

    @Override
    public void initialize(ValidCond validCond) {

    }

    @Override
    public boolean isValid(Cond value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return !StrUtil.isBlank(value.getCol()) || (value.getConds() != null && !value.getConds().isEmpty());
    }
}
