package io.github.pengxianggui.crud.join;

import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ServiceLoader;

/**
 * @author pengxg
 * @date 2025/12/2 15:46
 */
@Configuration
public class MethodReferenceLoaderConfig {
    @PostConstruct
    public void init() {
        // 使用Java原生SPI加载所有生成的方法引用加载器
        ServiceLoader<MethodReferenceLoader> loader = ServiceLoader.load(MethodReferenceLoader.class);
        for (MethodReferenceLoader registrar : loader) {
            // 执行注册逻辑
            registrar.load();
        }
    }
}
