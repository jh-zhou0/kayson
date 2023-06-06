package cn.zjh.kayson.framework.idempotent.core.keyresolver;

import cn.zjh.kayson.framework.idempotent.core.annotation.Idempotent;
import org.aspectj.lang.JoinPoint;

/**
 * 幂等 Key 解析器接口
 * 
 * @author zjh - kayson
 */
public interface IdempotentKeyResolver {

    /**
     * 解析一个 Key
     *
     * @param joinPoint  AOP 切面
     * @param idempotent 幂等注解
     * @return Key
     */
    String resolver(JoinPoint joinPoint, Idempotent idempotent);
    
}
