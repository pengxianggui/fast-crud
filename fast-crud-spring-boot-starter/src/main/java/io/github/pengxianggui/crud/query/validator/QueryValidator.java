package io.github.pengxianggui.crud.query.validator;

import cn.hutool.core.collection.CollectionUtil;
import io.github.pengxianggui.crud.query.Query;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Slf4j
public class QueryValidator implements ConstraintValidator<ValidQuery, Query> {

    @Override
    public void initialize(ValidQuery validQuery) {

    }

    @Override
    public boolean isValid(Query query, ConstraintValidatorContext context) {
        if (query == null) {
            return true;
        }

        return !existColOfOrderNotInCols(query);
    }

    private boolean existColOfOrderNotInCols(Query query) {
        if (CollectionUtil.isEmpty(query.getOrders()) || CollectionUtil.isEmpty(query.getCols())) {
            return false;
        }

        return !query.getOrders().stream().allMatch(order -> query.getCols().contains(order.getCol()));
    }
}
