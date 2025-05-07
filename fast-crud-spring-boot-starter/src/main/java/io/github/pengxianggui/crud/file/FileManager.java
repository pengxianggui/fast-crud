package io.github.pengxianggui.crud.file;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import io.github.pengxianggui.crud.FastCrudProperty;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pengxg
 * @date 2025/5/6 15:45
 */
@Slf4j
public class FileManager {
    private Map<String, FileService> registeredFileService = new HashMap<>();
    private static final String DEFAULT_MODE = LocalFileService.MODE;
    private FastCrudProperty.Upload upload;

    public FileManager(FastCrudProperty.Upload upload) {
        this.upload = upload;
    }

    @PostConstruct
    public void init() {
        Map<String, FileService> beansOfType = SpringUtil.getBeansOfType(FileService.class);
        for (FileService fileService : beansOfType.values()) {
            this.registeredFileService.put(fileService.getMode(), fileService);
            log.debug("registered file service : {}, mode:{}", fileService, fileService.getMode());
        }
    }

    public FileService getFileService() {
        return getFileService(StrUtil.blankToDefault(this.upload.getMode(), DEFAULT_MODE));
    }

    public FileService getFileService(String mode) {
        if (!registeredFileService.containsKey(mode)) {
            throw new RuntimeException("file service not found, mode:" + mode);
        }
        return registeredFileService.get(mode);
    }
}
