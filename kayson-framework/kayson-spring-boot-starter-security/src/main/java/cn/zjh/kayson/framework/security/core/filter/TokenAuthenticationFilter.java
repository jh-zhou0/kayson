package cn.zjh.kayson.framework.security.core.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.zjh.kayson.framework.common.exception.ServiceException;
import cn.zjh.kayson.framework.common.pojo.CommonResult;
import cn.zjh.kayson.framework.common.util.servlet.ServletUtils;
import cn.zjh.kayson.framework.security.LoginUser;
import cn.zjh.kayson.framework.security.config.SecurityProperties;
import cn.zjh.kayson.framework.security.core.util.SecurityFrameworkUtils;
import cn.zjh.kayson.framework.web.core.handler.GlobalExceptionHandler;
import cn.zjh.kayson.framework.web.core.util.WebFrameworkUtils;
import cn.zjh.kayson.module.system.api.oauth2.OAuth2TokenApi;
import cn.zjh.kayson.module.system.api.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
    
    private final OAuth2TokenApi oAuth2TokenApi;
    
    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 获取token
        String token = SecurityFrameworkUtils.obtainAuthorization(request, securityProperties.getTokenHeader());
        if (StrUtil.isNotEmpty(token)) {
            Integer loginUserType = WebFrameworkUtils.getLoginUserType(request);
            try {
                // 1.1 基于 token 构建登录用户
                LoginUser loginUser = buildLoginUserByToken(token, loginUserType);
                // 1.2 模拟 Login 功能，方便日常开发调试
                if (loginUser == null) {
                    loginUser = mockLoginUser(token, loginUserType);
                }
                // 2. 设置当前用户
                if (loginUser != null) {
                    SecurityFrameworkUtils.setLoginUser(loginUser, request);
                }
            } catch (Throwable ex) {
                CommonResult<?> result = globalExceptionHandler.allExceptionHandler(request, ex);
                ServletUtils.writeJSON(response, result);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private LoginUser buildLoginUserByToken(String token, Integer loginUserType) {
        try {
            // 校验token
            OAuth2AccessTokenCheckRespDTO accessToken = oAuth2TokenApi.checkAccessToken(token);
            if (accessToken == null) {
                return null;
            }
            // 用户类型不匹配，无权限
            if (ObjectUtil.notEqual(accessToken.getUserType(), loginUserType)) {
                throw new AccessDeniedException("错误的用户类型");
            }
            // 构建登录用户
            return new LoginUser()
                    .setId(accessToken.getUserId())
                    .setUserType(accessToken.getUserType())
                    .setScopes(accessToken.getScopes());
        } catch (ServiceException serviceException) {
            // 校验 Token 不通过时，考虑到一些接口是无需登录的，所以直接返回 null 即可
            return null;
        }
    }

    /**
     * 模拟登录用户，方便日常开发调试
     * 注意，在线上环境下，一定要关闭该功能！！！
     *
     * @param token 模拟的 token，格式为 {@link SecurityProperties#getMockSecret()} + 用户编号
     * @param userType 用户类型
     * @return 模拟的 LoginUser
     */
    private LoginUser mockLoginUser(String token, Integer userType) {
        if (!securityProperties.getMockEnable()) {
            return null;
        }
        // 必须以 mockSecret 开头
        if (!token.startsWith(securityProperties.getMockSecret())) {
            return null;
        }
        // 构建模拟用户
        // 构建模拟用户
        Long userId = Long.valueOf(token.substring(securityProperties.getMockSecret().length()));
        return new LoginUser().setId(userId).setUserType(userType);
    }
}
