package cn.zjh.kayson.framework.security.core.filter;

import cn.zjh.kayson.framework.security.LoginUser;
import cn.zjh.kayson.framework.security.config.SecurityProperties;
import cn.zjh.kayson.framework.web.core.handler.GlobalExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Token 过滤器，验证 token 的有效性
 * 验证通过后，获得 {@link LoginUser} 信息，并加入到 Spring Security 上下文
 * 
 * @author zjh - kayson
 */
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    
    private final SecurityProperties securityProperties;
    
    private final GlobalExceptionHandler globalExceptionHandler;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // TODO: 
        //  1.获取并校验token
        //  2.构建loginUser用户
        //  3.将登录用户设置到Security上下文中
        filterChain.doFilter(request, response);
    }
}
