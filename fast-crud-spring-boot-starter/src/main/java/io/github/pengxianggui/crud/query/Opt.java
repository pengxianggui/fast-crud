package io.github.pengxianggui.crud.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author pengxg
 * @date 2025-02-21 13:53
 */
public enum Opt {

    EQ("="),
    NE("!="),
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<="),
    IN("in"),
    NIN("nin"),
    LIKE("like"),
    LLIKE("llike"),
    RLIKE("rlike"),
    NLIKE("nlike"),
    NULL("null"),
    EMPTY("empty"), // 空值，包括null和空字符串, 注意: 空格组成的字符串不视为空值
    NNULL("nnull"),
    NEMPTY("nempty"); // 非空，不为null且不为空字符串。 与empty相反

    private String value;

    Opt(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Opt getOperationByValue(String value) {
        for (Opt opt : Opt.values()) {
            if (opt.value.equals(value)) {
                return opt;
            }
        }
        throw new IllegalArgumentException("参数异常: opt值只支持"
                + String.join(",", Arrays.stream(Opt.values()).map(Opt::getValue).collect(Collectors.toList())));
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}
