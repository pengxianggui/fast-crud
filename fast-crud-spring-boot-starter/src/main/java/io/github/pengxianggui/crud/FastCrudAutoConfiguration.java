package io.github.pengxianggui.crud;

import io.github.pengxianggui.crud.dynamic.DynamicCrudGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.validation.Validator;

/**
 * @author pengxg
 * @date 2024/11/30 14:27
 */
@Configuration
public class FastCrudAutoConfiguration {

//    @ConditionalOnBean(value = {RequestMappingHandlerMapping.class, Validator.class})
    @ConditionalOnMissingBean(DynamicCrudGenerator.class)
    @Bean
    public DynamicCrudGenerator dynamicCrudGenerator(RequestMappingHandlerMapping requestMappingHandlerMapping, Validator validator) {
        return new DynamicCrudGenerator(requestMappingHandlerMapping, validator);
    }
}
