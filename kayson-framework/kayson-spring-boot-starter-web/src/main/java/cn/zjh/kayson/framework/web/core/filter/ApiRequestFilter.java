package cn.zjh.kayson.framework.web.core.filter;

import cn.hutool.core.util.StrUtil;
import cn.zjh.kayson.framework.web.config.WebProperties;
import lombok.AllArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * 过滤 /admin-api、/app-api 等 API 请求的过滤器
 * 
 * @author zjh - kayson
 */
@AllArgsConstructor
public abstract class ApiRequestFilter extends OncePerRequestFilter {
    
    protected final WebProperties webProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // 只过滤 API 请求的地址
        return !StrUtil.startWithAny(request.getRequestURI(), webProperties.getAppApi().getPrefix(),
                webProperties.getAdminApi().getPrefix());
    }
    
}
