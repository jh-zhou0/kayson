package cn.zjh.kayson.framework.idempotent.config;

import cn.zjh.kayson.framework.idempotent.core.aop.IdempotentAspect;
import cn.zjh.kayson.framework.idempotent.core.keyresolver.IdempotentKeyResolver;
import cn.zjh.kayson.framework.idempotent.core.keyresolver.impl.DefaultIdempotentKeyResolver;
import cn.zjh.kayson.framework.idempotent.core.keyresolver.impl.ExpressionIdempotentKeyResolver;
import cn.zjh.kayson.framework.idempotent.core.redis.IdempotentRedisDAO;
import cn.zjh.kayson.framework.redis.config.KaysonRedisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * @author zjh - kayson
 */
@AutoConfiguration(after = KaysonRedisAutoConfiguration.class)
public class KaysonIdempotentConfiguration {

    @Bean
    public IdempotentRedisDAO idempotentRedisDAO(StringRedisTemplate redisTemplate) {
        return new IdempotentRedisDAO(redisTemplate);
    }
    
    @Bean
    public IdempotentAspect idempotentAspect(List<IdempotentKeyResolver> keyResolvers, IdempotentRedisDAO idempotentRedisDAO) {
        return new IdempotentAspect(keyResolvers, idempotentRedisDAO);
    }

    // ========== 各种 IdempotentKeyResolver Bean ==========
    @Bean
    public DefaultIdempotentKeyResolver defaultIdempotentKeyResolver() {
        return new DefaultIdempotentKeyResolver();
    }
    
    @Bean
    public ExpressionIdempotentKeyResolver expressionIdempotentKeyResolver() {
        return new ExpressionIdempotentKeyResolver();
    }
    
}
