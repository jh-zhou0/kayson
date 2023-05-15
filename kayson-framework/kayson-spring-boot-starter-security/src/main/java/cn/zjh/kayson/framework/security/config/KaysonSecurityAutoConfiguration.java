package cn.zjh.kayson.framework.security.config;

import cn.zjh.kayson.framework.security.core.context.TransmittableThreadLocalSecurityContextHolderStrategy;
import cn.zjh.kayson.framework.security.core.filter.TokenAuthenticationFilter;
import cn.zjh.kayson.framework.security.core.handler.AccessDeniedHandlerImpl;
import cn.zjh.kayson.framework.security.core.handler.AuthenticationEntryPointImpl;
import cn.zjh.kayson.framework.security.core.service.SecurityFrameworkService;
import cn.zjh.kayson.framework.security.core.service.SecurityFrameworkServiceImpl;
import cn.zjh.kayson.framework.web.core.handler.GlobalExceptionHandler;
import cn.zjh.kayson.module.system.api.oauth2.OAuth2TokenApi;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.annotation.Resource;

/**
 * Spring Security 自动配置类，主要用于相关组件的配置
 * 注意，不能和 {@link KaysonWebSecurityConfigurerAdapter} 用一个，原因是会导致初始化报错。
 * 
 * @author zjh - kayson
 */
@AutoConfiguration
@EnableConfigurationProperties(SecurityProperties.class)
public class KaysonSecurityAutoConfiguration {
    
    @Resource
    private SecurityProperties securityProperties;

    /**
     * 认证失败处理类 Bean
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new AuthenticationEntryPointImpl();
    }

    /**
     * 权限不够处理器 Bean
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandlerImpl();
    }

    /**
     * Spring Security 加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(securityProperties.getPasswordEncoderLength());
    }

    /**
     * Token 认证过滤器 Bean
     */
    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter(GlobalExceptionHandler globalExceptionHandler,
                                                               OAuth2TokenApi oAuth2TokenApi) {
        return new TokenAuthenticationFilter(securityProperties, globalExceptionHandler, oAuth2TokenApi);
    }

    @Bean("ss") // 使用 Spring Security 的缩写，方便使用
    public SecurityFrameworkService securityFrameworkService() {
        return new SecurityFrameworkServiceImpl();
    }

    /**
     * 声明调用 {@link SecurityContextHolder#setStrategyName(String)} 方法，
     * 设置使用 {@link TransmittableThreadLocalSecurityContextHolderStrategy} 作为 Security 的上下文策略
     */
    @Bean
    public MethodInvokingFactoryBean securityContextHolderMethodInvokingFactoryBean() {
        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
        methodInvokingFactoryBean.setTargetClass(SecurityContextHolder.class);
        methodInvokingFactoryBean.setTargetMethod("setStrategyName");
        methodInvokingFactoryBean.setArguments(TransmittableThreadLocalSecurityContextHolderStrategy.class.getName());
        return methodInvokingFactoryBean;
    }
    
}
