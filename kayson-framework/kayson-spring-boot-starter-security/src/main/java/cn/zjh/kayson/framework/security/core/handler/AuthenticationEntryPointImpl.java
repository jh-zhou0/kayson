package cn.zjh.kayson.framework.security.core.handler;

import cn.zjh.kayson.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.zjh.kayson.framework.common.pojo.CommonResult;
import cn.zjh.kayson.framework.common.util.servlet.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.ExceptionTranslationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 访问一个需要认证的 URL 资源，但是此时自己尚未认证（登录）的情况下，返回 {@link GlobalErrorCodeConstants#UNAUTHORIZED} 错误码，从而使前端重定向到登录页
 * 补充：Spring Security 通过 {@link ExceptionTranslationFilter#sendStartAuthentication(HttpServletRequest, HttpServletResponse, FilterChain, AuthenticationException)} 方法，调用当前类
 * 
 * @author zjh - kayson
 */
@Slf4j
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        log.debug("[commence][访问 URL({}) 时，没有登录]", request.getRequestURI(), e);
        // 返回 401
        ServletUtils.writeJSON(response, CommonResult.error(GlobalErrorCodeConstants.UNAUTHORIZED));
    }
    
}
