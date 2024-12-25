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
    private String uploadDir;
}
