package cn.zjh.kayson.framework.monitor.core.filter;

import cn.zjh.kayson.framework.common.util.montor.TracerUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Trace 过滤器，打印 traceId 到 header 中返回
 * 
 * @author zjh - kayson
 */
public class TracerFilter extends OncePerRequestFilter {

    /**
     * Header 名 - 链路追踪编号
     */
    private static final String HEADER_NAME_TRACE_ID = "trace-id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 设置响应 traceId
        request.setAttribute(HEADER_NAME_TRACE_ID, TracerUtils.getTraceId());
        // 继续过滤
        filterChain.doFilter(request, response);
    }
    
}
