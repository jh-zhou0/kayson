package cn.zjh.kayson.framework.monitor.config;

import cn.zjh.kayson.framework.common.enums.WebFilterOrderEnum;
import cn.zjh.kayson.framework.monitor.core.filter.TracerFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Tracer 配置类
 * 
 * @author zjh - kayson
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "kayson.tracer", value = "enable", matchIfMissing = true)
public class KaysonTracerAutoConfiguration {

    /**
     * 创建 TraceFilter 过滤器，响应 header 设置 traceId
     */
    @Bean
    public FilterRegistrationBean<TracerFilter> tracerFilter() {
        FilterRegistrationBean<TracerFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TracerFilter());
        registrationBean.setOrder(WebFilterOrderEnum.TRACE_FILTER);
        return registrationBean;
    }
    
}
