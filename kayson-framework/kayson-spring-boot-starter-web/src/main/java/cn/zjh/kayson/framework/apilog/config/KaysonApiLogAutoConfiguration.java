package cn.zjh.kayson.framework.apilog.config;

import cn.zjh.kayson.framework.apilog.core.filter.ApiAccessLogFilter;
import cn.zjh.kayson.framework.apilog.core.service.ApiAccessLogFrameworkService;
import cn.zjh.kayson.framework.apilog.core.service.ApiAccessLogFrameworkServiceImpl;
import cn.zjh.kayson.framework.apilog.core.service.ApiErrorLogFrameworkService;
import cn.zjh.kayson.framework.apilog.core.service.ApiErrorLogFrameworkServiceImpl;
import cn.zjh.kayson.framework.common.enums.WebFilterOrderEnum;
import cn.zjh.kayson.framework.web.config.KaysonWebAutoConfiguration;
import cn.zjh.kayson.framework.web.config.WebProperties;
import cn.zjh.kayson.module.infra.api.logger.ApiAccessLogApi;
import cn.zjh.kayson.module.infra.api.logger.ApiErrorLogApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * @author zjh - kayson
 */
@AutoConfiguration(after = KaysonWebAutoConfiguration.class)
public class KaysonApiLogAutoConfiguration {

    @Bean
    public ApiAccessLogFrameworkService apiAccessLogFrameworkService(ApiAccessLogApi apiAccessLogApi) {
        return new ApiAccessLogFrameworkServiceImpl(apiAccessLogApi);
    }
    
    @Bean
    public ApiErrorLogFrameworkService apiErrorLogFrameworkService(ApiErrorLogApi apiErrorLogApi) {
        return new ApiErrorLogFrameworkServiceImpl(apiErrorLogApi);
    }

    /**
     * 创建 ApiAccessLogFilter Bean，记录 API 请求日志
     */
    @Bean
    @ConditionalOnProperty(prefix = "kayson.access-log", value = "enable", matchIfMissing = true) // 允许使用 kayson.access-log.enable=false 禁用访问日志
    public FilterRegistrationBean<ApiAccessLogFilter> apiAccessLogFilter(WebProperties webProperties,
                                                                         @Value("${spring.application.name}") String applicationName,
                                                                         ApiAccessLogFrameworkService apiAccessLogFrameworkService) {
        ApiAccessLogFilter apiAccessLogFilter = new ApiAccessLogFilter(webProperties, applicationName, apiAccessLogFrameworkService);
        FilterRegistrationBean<ApiAccessLogFilter> bean = new FilterRegistrationBean<>(apiAccessLogFilter);
        bean.setOrder(WebFilterOrderEnum.API_ACCESS_LOG_FILTER);
        return bean;
    }
}
