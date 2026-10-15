package io.github.pengxianggui.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pengxianggui.crud.apidoc.SwaggerConfig;
import io.github.pengxianggui.crud.batch.BatchUpdateAuditRecorder;
import io.github.pengxianggui.crud.batch.BatchUpdateExceptionHandler;
import io.github.pengxianggui.crud.batch.JdbcBatchUpdateAuditRecorder;
import io.github.pengxianggui.crud.batch.LoggingBatchUpdateAuditRecorder;
import io.github.pengxianggui.crud.config.MybatisConfig;
import io.github.pengxianggui.crud.config.RequestMappingAutoRemover;
import io.github.pengxianggui.crud.download.FileResourceHttpRequestHandler;
import io.github.pengxianggui.crud.file.FileConfig;
import io.github.pengxianggui.crud.join.MethodReferenceLoaderConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.sql.DataSource;

/**
 * @author pengxg
 * @date 2024/11/30 14:27
 */
@ImportAutoConfiguration({MybatisConfig.class, FileConfig.class, SwaggerConfig.class, MethodReferenceLoaderConfig.class})
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

    /**
     * 批量修改的审计输出。默认写入 fc_batch_update_log 表；
     * 若使用方声明了自己的 {@link BatchUpdateAuditRecorder} 则优先生效；
     * 若无数据源则退化为打日志。
     */
    @ConditionalOnMissingBean(BatchUpdateAuditRecorder.class)
    @ConditionalOnClass(JdbcTemplate.class)
    @Bean
    public BatchUpdateAuditRecorder batchUpdateAuditRecorder(JdbcTemplate jdbcTemplate,
                                                             ObjectMapper objectMapper) {
        return new JdbcBatchUpdateAuditRecorder(jdbcTemplate, objectMapper);
    }

    @ConditionalOnMissingBean(BatchUpdateAuditRecorder.class)
    @Bean
    public BatchUpdateAuditRecorder loggingBatchUpdateAuditRecorder() {
        return new LoggingBatchUpdateAuditRecorder();
    }

    /**
     * 把批量修改的校验失败（字段不在白名单、值类型不合法等）映射为可读的 HTTP 400。
     * 它是全局 Advice，会作用于使用方的所有 Controller，因此默认不注册；
     * 需要时显式开启：fast-crud.batch-update.map-validation-error=true。
     * 关闭时校验失败会抛异常，由使用方自己的全局异常处理接管。
     */
    @ConditionalOnProperty(name = "fast-crud.batch-update.map-validation-error", havingValue = "true")
    @ConditionalOnMissingBean(BatchUpdateExceptionHandler.class)
    @Bean
    public BatchUpdateExceptionHandler batchUpdateExceptionHandler() {
        return new BatchUpdateExceptionHandler();
    }
}
