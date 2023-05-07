package cn.zjh.kayson.framework.web.core.handler;

import cn.zjh.kayson.framework.common.pojo.CommonResult;
import cn.zjh.kayson.framework.web.core.util.WebFrameworkUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局响应结果（ResponseBody）处理器
 * 
 * 目前，GlobalResponseBodyHandler 的主要作用是，记录 Controller 的返回结果，
 * 方便记录 API 访问日志
 * 
 * @author zjh - kayson
 */
@ControllerAdvice
public class GlobalResponseBodyHandler implements ResponseBodyAdvice<Object> {
    
    @Override
    @SuppressWarnings("NullableProblems") // 避免 IDEA 警告
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (returnType.getMethod() == null) {
            return false;
        }
        // 只拦截返回结果为 CommonResult 类型
        return returnType.getMethod().getReturnType() == CommonResult.class;
    }

    @Override
    @SuppressWarnings("NullableProblems") // 避免 IDEA 警告
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, 
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, 
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // 记录 Controller 结果
        ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
        HttpServletRequest servletRequest = servletServerHttpRequest.getServletRequest();
        WebFrameworkUtils.setCommonResult(servletRequest, (CommonResult<?>) body);
        return body;
    }
}
