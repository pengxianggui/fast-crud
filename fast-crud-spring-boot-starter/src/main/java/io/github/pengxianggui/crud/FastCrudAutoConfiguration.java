package io.github.pengxianggui.crud;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import io.github.pengxianggui.crud.apidoc.SwaggerConfig;
import io.github.pengxianggui.crud.download.FileResourceHttpRequestHandler;
import io.github.pengxianggui.crud.config.RequestMappingAutoRemover;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author pengxg
 * @date 2024/11/30 14:27
 */
@ImportAutoConfiguration(SwaggerConfig.class)
@EnableConfigurationProperties(FastCrudProperty.class)
@Configuration
public class FastCrudAutoConfiguration {

    @ConditionalOnBean(RequestMappingHandlerMapping.class)
    @Bean
    public RequestMappingAutoRemover dynamicRequestMappingRemover(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        return new RequestMappingAutoRemover(requestMappingHandlerMapping);
    }

    /**
     * 添加分页插件
     */
    @ConditionalOnMissingBean(MybatisPlusInterceptor.class)
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 如果配置多个插件, 切记分页最后添加
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
        return interceptor;
    }

    @Bean
    public FileResourceHttpRequestHandler fileResourceHttpRequestHandler() {
        return new FileResourceHttpRequestHandler();
    }
}
