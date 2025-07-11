package io.github.pengxianggui.crud.file;

import io.github.pengxianggui.crud.FastCrudProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * @author pengxg
 * @date 2025/5/6 17:32
 */
@Configuration
public class FileConfig {

    @ConditionalOnMissingBean(LocalFileService.class)
    @Bean
    public LocalFileService localFileService(FastCrudProperty fastCrudProperty) {
        return new LocalFileService(
                Optional.ofNullable(fastCrudProperty.getUpload()).map(FastCrudProperty.Upload::getLocal).orElse(null)
        );
    }

    @ConditionalOnMissingBean(FileManager.class)
    @Bean
    public FileManager fileManager(FastCrudProperty fastCrudProperty) {
        return new FileManager(fastCrudProperty.getUpload());
    }

}
