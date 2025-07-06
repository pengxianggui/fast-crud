package io.github.pengxianggui.crud.apidoc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 默认的Swagger配置。注册了一个默认的Docket bean。主要为了在swagger接口文档中兑现{@link io.github.pengxianggui.crud.CrudExclude}注解
 *
 * @author pengxg
 * @date 2025-02-17 11:51
 */
@EnableSwagger2
@Configuration
@ConditionalOnClass(Docket.class)
public class SwaggerConfig {

    @Bean
    public CrudApiDocAutoRemovePredicate crudApiDocAutoRemovePredicate() {
        return new CrudApiDocAutoRemovePredicate();
    }
    
    @ConditionalOnMissingBean
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(crudApiDocAutoRemovePredicate())
                .build();
    }
}
