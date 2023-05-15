package cn.zjh.kayson.module.infra.framework.web;

import cn.zjh.kayson.framework.swagger.config.KaysonSwaggerAutoConfiguration;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zjh - kayson
 */
@Configuration(proxyBeanMethods = false)
public class InfraWebConfiguration {

    /**
     * infra 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi infraGroupedOpenApi() {
        return KaysonSwaggerAutoConfiguration.buildGroupedOpenApi("infra");
    }
}
