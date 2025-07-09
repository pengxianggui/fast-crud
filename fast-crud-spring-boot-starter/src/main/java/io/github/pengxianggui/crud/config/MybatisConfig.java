package io.github.pengxianggui.crud.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import io.github.pengxianggui.crud.join.CommonRepo;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author pengxg
 * @date 2025/7/5 14:21
 */
@ConditionalOnClass({SqlSession.class, SqlSessionFactory.class})
@Configuration
public class MybatisConfig {

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
    public CommonRepo commonRepo() {
        return new CommonRepo();
    }

    @Bean
    public MapperResolver mapperResolver(SqlSessionFactory sqlSessionFactory) {
        return new MapperResolver(sqlSessionFactory);
    }
}
