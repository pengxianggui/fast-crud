package io.github.pengxianggui.crud;

public enum CrudMethod {
    INSERT("_insert"),
    INSERT_BATCH("_insertBatch"),
    UPDATE("_update"),
    UPDATE_BATCH("_updateBatch"),
    LIST("_list"),
    PAGE("_page"),
    DETAIL("_detail"),
    DELETE("_delete"),
    DELETE_BATCH("_deleteBatch"),
    EXISTS("_exists"),
    UPLOAD("_upload"),
    DOWNLOAD("_download"),
    EXPORT("_export");

    private String name;

    CrudMethod(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
