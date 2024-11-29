package io.github.pengxianggui.crud.query.validator;

import io.github.pengxianggui.crud.query.Order;
import io.github.pengxianggui.crud.query.Query;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class QueryValidator implements ConstraintValidator<ValidQuery, Query> {

    @Override
    public void initialize(ValidQuery validQuery) {

    }

    @Override
    public boolean isValid(Query value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value.getCols() == null || value.getCols().length == 0) {
            return true;
        }
        if (value.getOrders() == null || value.getOrders().isEmpty()) {
            return true;
        }

        for (Order order : value.getOrders()) {
            for (String col : value.getCols()) {
                if (col.equals(order.getCol())) {
                    return true;
                }
            }
        }
        return false;
    }
}
