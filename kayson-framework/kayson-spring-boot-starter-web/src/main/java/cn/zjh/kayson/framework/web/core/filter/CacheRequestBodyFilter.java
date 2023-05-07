package cn.zjh.kayson.framework.web.core.filter;

import cn.zjh.kayson.framework.common.util.servlet.ServletUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Request Body 缓存 Filter，实现它的可重复读取
 * 
 * @author zjh - kayson
 */
public class CacheRequestBodyFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(new CacheRequestBodyWrapper(request), response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // 只处理 json 请求内容，返回false过滤，返回true不过滤
        return !ServletUtils.isJsonRequest(request);
    }
}
