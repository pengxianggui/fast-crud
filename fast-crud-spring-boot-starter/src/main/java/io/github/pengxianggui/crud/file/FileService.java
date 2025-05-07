package io.github.pengxianggui.crud.file;

import com.google.common.io.Files;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author pengxg
 * @date 2025/5/6 17:12
 */
public interface FileService {
    String getMode();

    /**
     * 上传文件。当使用DbMeta基于元数据生成的文件上传控件时会调用此方法进行文件保存
     *
     * @param file         文件
     * @param splitMarkers 分隔标记，分隔目录。你可以依据用于文件存放管理
     * @return 返回文件地址, 可以是文件路径，也可以是绝对路径(或者http://xxx)。
     */
    String upload(MultipartFile file, String... splitMarkers) throws IOException;

    /**
     * 文件名转换。提供上传的原始文件，返回存储的文件名，为了防止文件重复。采用添加后缀：yyyyMMdd_HH_mm_ss_SSS。如:
     *
     * <pre>
     * filename  abcd.txt
     * newname   abcd_yyyy-MM-dd_HH:mm:ssSSS.txt
     * </pre>
     *
     * @param file 上传的原始文件
     * @return 带有时间格式后缀的文件名
     */
    default String getFileNameWithAffix(MultipartFile file) {
        return Files.getNameWithoutExtension(file.getOriginalFilename()) + "_" + new SimpleDateFormat("yyyyMMdd_HH_mm_ss_SSS").format(new Date()) + "."
                + Files.getFileExtension(file.getOriginalFilename());
    }

    /**
     * 获取文件。获取指定资源的文件。
     *
     * @param fileUrl 当此文件是通过{@link #upload(MultipartFile, String...)}上传的，那么此值通常为后者的返回值， 它可能是一个本地相对路径，也可能是一个绝对路径(http://...), 全由继承类去决定。
     * @return 返回文件对象
     */
    File getFile(String fileUrl);
}
