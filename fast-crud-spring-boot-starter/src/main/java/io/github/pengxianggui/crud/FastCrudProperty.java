package io.github.pengxianggui.crud;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author pengxg
 * @date 2024-12-25 9:21
 */
@ConfigurationProperties(prefix = "fast-crud")
@Data
public class FastCrudProperty {

    @Deprecated
    private String uploadDir;
    private String host;
    private Upload upload;

    @Data
    public static class Upload {
        private String mode;
        private Local local;
    }

    @Data
    public static class Local {
        private String dir;
    }
}
