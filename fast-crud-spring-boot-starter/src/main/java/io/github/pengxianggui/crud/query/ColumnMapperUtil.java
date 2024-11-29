package io.github.pengxianggui.crud.query;

public class ColumnMapperUtil {

    public static volatile String QUOTE = "";

    public static String map(String col) {
        col = col.replaceAll("[^\\w\\.]", "");
        if (col.contains(".")) {
            return col;
        }
        return QUOTE + col + QUOTE;
    }

}
