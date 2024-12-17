package io.github.pengxianggui.crud.query;

import cn.hutool.core.lang.Assert;
import io.github.pengxianggui.crud.meta.EntityUtil;

public class ColumnMapperUtil {

    public static volatile String QUOTE = "`";

    /**
     * 列属性转换为数据库字段(如果entityClazz != null的话), 并用`包裹
     *
     * @param col
     * @param entityClazz
     * @return
     */
    public static String toDbField(String col, Class<?> entityClazz) {
        String dbCol = col;
        if (entityClazz != null) {
            dbCol = EntityUtil.getDbFieldName(entityClazz, col);
            Assert.notNull(dbCol, "请检查字段是否正确：" + col + ", 并确保类(" + entityClazz.getName() + ")中含有此字段。");
        }
        return wrapper(dbCol);
    }

    public static String wrapper(String dbCol) {
        if (ifWrapper(dbCol)) {
            return dbCol;
        }
        return QUOTE + dbCol + QUOTE;
    }

    private static boolean ifWrapper(String col) {
        Assert.notNull(col, "col is not null!");
        return col.startsWith(QUOTE) && col.endsWith(QUOTE);
    }

}
