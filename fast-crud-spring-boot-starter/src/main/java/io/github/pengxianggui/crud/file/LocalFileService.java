package io.github.pengxianggui.crud.file;

import com.google.common.base.Joiner;
import io.github.pengxianggui.crud.FastCrudProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author pengxg
 * @date 2025/5/6 17:14
 */
@Slf4j
public class LocalFileService implements FileService {
    public static final String MODE = "local";
    private FastCrudProperty.Local local;

    public LocalFileService(FastCrudProperty.Local local) {
        this.local = local;
    }

    @Override
    public String getMode() {
        return MODE;
    }

    @Override
    public String upload(MultipartFile file, String... splitMarkers) throws IOException {
        String destPath = Joiner.on(File.separator).skipNulls().join(splitMarkers);
        File targetFile = toTargetFile(local.getDir() + File.separator + destPath, file);
        return targetFile.getAbsolutePath();
    }

    @Override
    public File getFile(String fileUrl) {
        String path = Paths.get(fileUrl).toString();
        return new File(path);
    }


    private File toTargetFile(String dirPath, MultipartFile file) throws IOException {
        if (!dirPath.endsWith(File.separator)) {
            dirPath += File.separator;
        }
        String fileName = getFileNameWithAffix(file);
        log.info("new file name :{}", fileName);
        File targetFile = new File(dirPath + fileName);
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        file.transferTo(targetFile);
        log.debug("destFile.getPath : {}", targetFile.getPath());
        return targetFile;
    }
}
