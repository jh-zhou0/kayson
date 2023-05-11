package cn.zjh.kayson.framework.redis.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author zjh - kayson
 */
@AutoConfiguration
public class KaysonRedisAutoConfiguration {

    /**
     * 创建 RedisTemplate Bean，使用 JSON 序列化方式
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 创建 RedisTemplate 对象
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置 RedisConnection 工厂。😈 它就是实现多种 Java Redis 客户端接入的秘密工厂。
        redisTemplate.setConnectionFactory(factory);
        // 使用 String 序列化方式，序列化 KEY
        redisTemplate.setKeySerializer(RedisSerializer.json());
        redisTemplate.setHashKeySerializer(RedisSerializer.json());
        // 使用 JSON 序列化方式（库是 Jackson ），序列化 VALUE
        redisTemplate.setValueSerializer(RedisSerializer.json());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        return redisTemplate;
    }
}
