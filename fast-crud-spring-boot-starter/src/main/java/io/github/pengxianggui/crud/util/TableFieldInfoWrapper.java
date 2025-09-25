package io.github.pengxianggui.crud.util;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import lombok.Getter;

/**
 * @author pengxg
 * @date 2025/9/24 17:16
 */
public class TableFieldInfoWrapper {

    @Getter
    private boolean charSequence;
    @Getter
    private String column;


    public static TableFieldInfoWrapper byPk(TableInfo tableInfo) {
        TableFieldInfoWrapper wrapper = new TableFieldInfoWrapper();
        wrapper.charSequence = (tableInfo.getKeyType()).isAssignableFrom(CharSequence.class);
        wrapper.column = tableInfo.getKeyColumn();
        return wrapper;
    }

    public static TableFieldInfoWrapper byField(TableFieldInfo tableFieldInfo) {
        if (tableFieldInfo == null) {
            return null;
        }
        TableFieldInfoWrapper wrapper = new TableFieldInfoWrapper();
        wrapper.charSequence = tableFieldInfo.isCharSequence();
        wrapper.column = tableFieldInfo.getColumn();
        return wrapper;
    }
}
