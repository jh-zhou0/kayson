package cn.zjh.kayson.framework.idempotent.core.redis;

import cn.zjh.kayson.framework.redis.core.RedisKeyDefine;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 幂等 Redis DAO
 * 
 * @author zjh - kayson
 */
@AllArgsConstructor
public class IdempotentRedisDAO {
    
    private final StringRedisTemplate redisTemplate;
    
    private static final RedisKeyDefine IDEMPOTENT = new RedisKeyDefine("幂等操作", 
            "idempotent:%s", // 参数为 uuid
            RedisKeyDefine.KeyTypeEnum.STRING, String.class, 
            RedisKeyDefine.TimeoutTypeEnum.DYNAMIC);
    
    public Boolean setIfAbsent(String key, long timeout, TimeUnit timeUnit) {
        String redisKey = String.format(IDEMPOTENT.getKeyTemplate(), key);
        return redisTemplate.opsForValue().setIfAbsent(redisKey, "", timeout, timeUnit);
    }
    
}
