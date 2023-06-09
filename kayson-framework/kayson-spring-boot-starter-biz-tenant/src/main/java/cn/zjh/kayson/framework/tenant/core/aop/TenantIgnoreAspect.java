package cn.zjh.kayson.framework.tenant.core.aop;

import cn.zjh.kayson.framework.tenant.core.context.TenantContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * @author zjh - kayson
 */
@Aspect
public class TenantIgnoreAspect {

    @Around("@annotation(tenantIgnore)")
    public Object around(ProceedingJoinPoint joinPoint, TenantIgnore tenantIgnore) throws Throwable {
        Boolean oldIgnore = TenantContextHolder.isIgnore();
        try {
            TenantContextHolder.setIgnore(false);
            // 执行逻辑
            return joinPoint.proceed();
        } finally {
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }
    
}
