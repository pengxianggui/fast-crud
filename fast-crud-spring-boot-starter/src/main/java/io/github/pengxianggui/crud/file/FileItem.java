package io.github.pengxianggui.crud.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传时文件对象
 *
 * @author pengxg
 * @date 2025/6/16 14:38
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileItem {
    /**
     * 文件名
     */
    private String name;
    /**
     * 文件url
     */
    private String url;
}
