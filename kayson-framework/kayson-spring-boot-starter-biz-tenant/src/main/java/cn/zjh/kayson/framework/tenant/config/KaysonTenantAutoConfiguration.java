package cn.zjh.kayson.framework.tenant.config;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.zjh.kayson.framework.common.enums.WebFilterOrderEnum;
import cn.zjh.kayson.framework.mybatis.core.util.MyBatisUtils;
import cn.zjh.kayson.framework.quartz.core.handler.JobHandler;
import cn.zjh.kayson.framework.tenant.core.aop.TenantIgnoreAspect;
import cn.zjh.kayson.framework.tenant.core.db.TenantDatabaseInterceptor;
import cn.zjh.kayson.framework.tenant.core.job.TenantJob;
import cn.zjh.kayson.framework.tenant.core.job.TenantJobHandlerDecorator;
import cn.zjh.kayson.framework.tenant.core.mq.TenantRedisMessageInterceptor;
import cn.zjh.kayson.framework.tenant.core.redis.TenantRedisCacheManager;
import cn.zjh.kayson.framework.tenant.core.security.TenantSecurityWebFilter;
import cn.zjh.kayson.framework.tenant.core.service.TenantFrameworkService;
import cn.zjh.kayson.framework.tenant.core.web.TenantContextWebFilter;
import cn.zjh.kayson.framework.web.config.WebProperties;
import cn.zjh.kayson.framework.web.core.handler.GlobalExceptionHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;

/**
 * @author zjh - kayson
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "kayson.tenant", value = "enable", matchIfMissing = true) // 允许使用 kayson.tenant.enable=false 禁用多租户
@EnableConfigurationProperties(TenantProperties.class)
public class KaysonTenantAutoConfiguration {

    // AOP
    @Bean
    public TenantIgnoreAspect tenantIgnoreAspect() {
        return new TenantIgnoreAspect();
    }
    
    // DB
    @Bean
    public TenantLineInnerInterceptor tenantLineInnerInterceptor(TenantProperties properties,
                                                                 MybatisPlusInterceptor interceptor) {
        TenantLineInnerInterceptor inner = new TenantLineInnerInterceptor(new TenantDatabaseInterceptor(properties));
        // 添加到 interceptor 中
        // 需要加在首个，主要是为了在分页插件前面。这个是 MyBatis Plus 的规定
        MyBatisUtils.addInterceptor(interceptor, inner, 0);
        return inner;
    }
    
    // Redis
    @Bean
    @Primary // 引入租户时，tenantRedisCacheManager 为主 Bean
    public RedisCacheManager tenantRedisCacheManager(RedisTemplate<String, Object> redisTemplate,
                                                     RedisCacheConfiguration redisCacheConfiguration) {
        // 创建 RedisCacheWriter 对象
        RedisConnectionFactory connectionFactory = Objects.requireNonNull(redisTemplate.getConnectionFactory());
        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
        // 创建 TenantRedisCacheManager 对象
        return new TenantRedisCacheManager(cacheWriter, redisCacheConfiguration);
    }
    
    // Web
    @Bean
    public FilterRegistrationBean<TenantContextWebFilter> tenantContextWebFilter() {
        FilterRegistrationBean<TenantContextWebFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TenantContextWebFilter());
        registrationBean.setOrder(WebFilterOrderEnum.TENANT_CONTEXT_FILTER);
        return registrationBean;
    }
    
    // Security
    @Bean
    public FilterRegistrationBean<TenantSecurityWebFilter> tenantSecurityWebFilter(WebProperties webProperties, 
                                                                                   TenantProperties tenantProperties,
                                                                                   GlobalExceptionHandler globalExceptionHandler,
                                                                                   TenantFrameworkService tenantFrameworkService) {
        FilterRegistrationBean<TenantSecurityWebFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TenantSecurityWebFilter(webProperties, tenantProperties, 
                globalExceptionHandler, tenantFrameworkService));
        registrationBean.setOrder(WebFilterOrderEnum.TENANT_SECURITY_FILTER);
        return registrationBean;
    }
    
    // Job
    @Bean
    public BeanPostProcessor jobHandlerBeanPostProcessor(TenantFrameworkService tenantFrameworkService) {
        
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (!(bean instanceof JobHandler)) {
                    return bean;
                }
                // 有 TenantJob 注解的情况下，才会进行处理
                if (!AnnotationUtil.hasAnnotation(bean.getClass(), TenantJob.class)) {
                    return bean;
                }
                // 使用 TenantJobHandlerDecorator 装饰
                return new TenantJobHandlerDecorator(tenantFrameworkService, (JobHandler) bean);
            }
        };
        
    }
    
    // MQ
    @Bean
    public TenantRedisMessageInterceptor tenantRedisMessageInterceptor() {
        return new TenantRedisMessageInterceptor();
    }
    
}
