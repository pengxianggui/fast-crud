package io.github.pengxianggui.crud.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author pengxg
 * @date 2025-02-21 13:55
 */
public enum Rel {

    AND("and"), OR("or");

    private String value;

    Rel(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Rel getRelationByValue(String value) {
        for (Rel rel : Rel.values()) {
            if (rel.value.equals(value)) {
                return rel;
            }
        }
        throw new IllegalArgumentException("参数异常: rel值只支持"
                + String.join(",", Arrays.stream(Rel.values()).map(Rel::getValue).collect(Collectors.toList())));
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}
