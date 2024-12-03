package io.github.pengxianggui.crud;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
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

    @ConditionalOnMissingBean(DynamicCrudGenerator.class)
    @Bean
    public DynamicCrudGenerator dynamicCrudGenerator(RequestMappingHandlerMapping requestMappingHandlerMapping, Validator validator) {
        return new DynamicCrudGenerator(requestMappingHandlerMapping, validator);
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
}
