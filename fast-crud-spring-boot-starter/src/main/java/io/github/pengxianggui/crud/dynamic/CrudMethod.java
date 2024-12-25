package io.github.pengxianggui.crud.dynamic;

public enum CrudMethod {
    INSERT("insert"),
    INSERT_BATCH("insertBatch"),
    UPDATE("update"),
    UPDATE_BATCH("updateBatch"),
    LIST("list"),
    PAGE("page"),
    DETAIL("detail"),
    DELETE("delete"),
    DELETE_BATCH("deleteBatch"),
    EXISTS("exists"),
    UPLOAD("upload"),
    DOWNLOAD("download"),
    EXPORT("export");

    private String name;

    CrudMethod(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
