package io.github.pengxianggui.crud;

import io.github.pengxianggui.crud.apidoc.SwaggerConfig;
import io.github.pengxianggui.crud.config.MybatisConfig;
import io.github.pengxianggui.crud.config.RequestMappingAutoRemover;
import io.github.pengxianggui.crud.download.FileResourceHttpRequestHandler;
import io.github.pengxianggui.crud.file.FileConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.sql.DataSource;

/**
 * @author pengxg
 * @date 2024/11/30 14:27
 */
@ImportAutoConfiguration({MybatisConfig.class, FileConfig.class, SwaggerConfig.class})
@EnableConfigurationProperties(FastCrudProperty.class)
@Configuration
public class FastCrudAutoConfiguration {

    @ConditionalOnBean(RequestMappingHandlerMapping.class)
    @Bean
    public RequestMappingAutoRemover dynamicRequestMappingRemover(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        return new RequestMappingAutoRemover(requestMappingHandlerMapping);
    }

    @Bean
    public FileResourceHttpRequestHandler fileResourceHttpRequestHandler() {
        return new FileResourceHttpRequestHandler();
    }

    @ConditionalOnMissingBean
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
