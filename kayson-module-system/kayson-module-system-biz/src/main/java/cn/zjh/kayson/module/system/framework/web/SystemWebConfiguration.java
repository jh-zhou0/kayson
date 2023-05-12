package cn.zjh.kayson.module.system.framework.web;

import cn.zjh.kayson.framework.swagger.config.KaysonSwaggerAutoConfiguration;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * system 模块的 web 组件的 Configuration
 * 
 * @author zjh - kayson
 */
@Configuration(proxyBeanMethods = false)
public class SystemWebConfiguration {

    /**
     * system 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi systemGroupedOpenApi() {
        return KaysonSwaggerAutoConfiguration.buildGroupedOpenApi("system");
    }
}
