package io.github.pengxianggui.crud;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author pengxg
 * @date 2024-12-25 9:21
 */
@ConfigurationProperties(prefix = "fast-crud")
@Data
public class FastCrudProperty {
    private String host = "http://localhost:8080";
    private Upload upload = new Upload("local", new Local(System.getProperty("user.dir") + "/upload"));

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Upload {
        private String mode;
        private Local local;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Local {
        private String dir;
    }
}
