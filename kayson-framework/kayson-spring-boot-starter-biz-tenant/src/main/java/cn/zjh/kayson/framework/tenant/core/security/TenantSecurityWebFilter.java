package cn.zjh.kayson.framework.tenant.core.security;

import cn.hutool.core.collection.CollUtil;
import cn.zjh.kayson.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.zjh.kayson.framework.common.pojo.CommonResult;
import cn.zjh.kayson.framework.common.util.servlet.ServletUtils;
import cn.zjh.kayson.framework.security.LoginUser;
import cn.zjh.kayson.framework.security.core.util.SecurityFrameworkUtils;
import cn.zjh.kayson.framework.tenant.config.TenantProperties;
import cn.zjh.kayson.framework.tenant.core.context.TenantContextHolder;
import cn.zjh.kayson.framework.tenant.core.service.TenantFrameworkService;
import cn.zjh.kayson.framework.web.config.WebProperties;
import cn.zjh.kayson.framework.web.core.filter.ApiRequestFilter;
import cn.zjh.kayson.framework.web.core.handler.GlobalExceptionHandler;
import cn.zjh.kayson.framework.web.core.util.WebFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * 多租户 Security Web 过滤器
 * 1.如果是登陆的用户，校验是否有权限访问该租户，避免越权问题。
 * 2.如果未登录，判断是否是忽略的 url
 *  2.1 是，若未传递租户编号，则默认忽略租户编号，避免报错
 *  2.2 不是，判断是否携带tenant_id，校验租户是合法，例如说被禁用、到期
 * 
 * @author zjh - kayson
 */
@Slf4j
public class TenantSecurityWebFilter extends ApiRequestFilter {
    
    private final TenantProperties tenantProperties;
    
    private final GlobalExceptionHandler globalExceptionHandler;
    
    private final AntPathMatcher pathMatcher;
    
    private final TenantFrameworkService tenantFrameworkService;

    public TenantSecurityWebFilter(WebProperties webProperties,
                                   TenantProperties tenantProperties,
                                   GlobalExceptionHandler globalExceptionHandler,
                                   TenantFrameworkService tenantFrameworkService) {
        super(webProperties);
        this.tenantProperties = tenantProperties;
        this.globalExceptionHandler = globalExceptionHandler;
        this.pathMatcher = new AntPathMatcher();
        this.tenantFrameworkService = tenantFrameworkService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Long tenantId = WebFrameworkUtils.getTenantId(request);
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        
        // 1.如果是登陆的用户，校验是否有权限访问该租户，避免越权问题。
        if (loginUser != null) {
            // 如果获取不到租户编号，则尝试使用登陆用户的租户编号
            if (tenantId == null) {
                tenantId = loginUser.getTenantId();
                TenantContextHolder.setTenantId(tenantId);
            // 如果传递了租户编号，则进行比对租户编号，避免越权问题
            } else if (!Objects.equals(loginUser.getTenantId(), tenantId)){
                log.error("[doFilterInternal][租户({}) User({}/{}) 越权访问租户({}) URL({}/{})]",
                        loginUser.getTenantId(), loginUser.getId(), loginUser.getUserType(),
                        TenantContextHolder.getTenantId(), request.getRequestURI(), request.getMethod());
                ServletUtils.writeJSON(response, CommonResult.error(GlobalErrorCodeConstants.FORBIDDEN.getCode(),
                        "您无权访问该租户的数据"));
                return;
            }
        }
        
        // 2.如果未登录，判断是否是忽略的 url
        if (isIgnoreUrl(request)) {
            // 如果是允许忽略租户的 URL，若未传递租户编号，则默认忽略租户编号，避免报错
            if (tenantId == null) {
                TenantContextHolder.setIgnore(true);
            }
        } else { // 如果非允许忽略租户的 URL，则校验租户是否合法
            // 如果请求未带租户的编号，不允许访问。
            if (tenantId == null) {
                log.error("[doFilterInternal][URL({}/{}) 未传递租户编号]", request.getRequestURI(), request.getMethod());
                ServletUtils.writeJSON(response, CommonResult.error(GlobalErrorCodeConstants.BAD_REQUEST.getCode(),
                        "请求的租户标识未传递，请进行排查"));
                return;
            }
            // 校验租户是合法，例如说被禁用、到期
            try {
                tenantFrameworkService.validateTenant(tenantId);
            } catch (Throwable ex) {
                CommonResult<?> result = globalExceptionHandler.allExceptionHandler(request, ex);
                ServletUtils.writeJSON(response, result);
                return;
            }
        }
        
        // 继续过滤
        filterChain.doFilter(request, response);
    }

    private boolean isIgnoreUrl(HttpServletRequest request) {
        // 快速匹配，保证性能
        if (CollUtil.contains(tenantProperties.getIgnoreUrls(), request.getRequestURI())) {
            return true;
        }
        // 逐个 Ant 路径匹配
        for (String url : tenantProperties.getIgnoreUrls()) {
            if (pathMatcher.match(url, request.getRequestURI())) {
                return true;
            }
        }
        return false;
    }
    
}
