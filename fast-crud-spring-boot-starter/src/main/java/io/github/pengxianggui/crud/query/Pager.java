package io.github.pengxianggui.crud.query;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.NoArgsConstructor;

@Deprecated
@NoArgsConstructor
@Data
public class Pager<T> extends Page<T> {

    public Pager(long current, long size) {
        super(current, size);
    }
}
