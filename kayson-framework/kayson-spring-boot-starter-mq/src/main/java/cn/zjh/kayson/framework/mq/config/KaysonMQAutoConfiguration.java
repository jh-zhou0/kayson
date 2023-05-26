package cn.zjh.kayson.framework.mq.config;

import cn.zjh.kayson.framework.mq.core.RedisMQTemplate;
import cn.zjh.kayson.framework.mq.core.interceptor.RedisMessageInterceptor;
import cn.zjh.kayson.framework.mq.core.pubsub.AbstractChannelMessageListener;
import cn.zjh.kayson.framework.redis.config.KaysonRedisAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.List;

/**
 * @author zjh - kayson
 */
@Slf4j
@AutoConfiguration(after = KaysonRedisAutoConfiguration.class)
public class KaysonMQAutoConfiguration {
    
    @Bean
    public RedisMQTemplate redisMQTemplate(StringRedisTemplate redisTemplate, List<RedisMessageInterceptor> interceptors) {
        RedisMQTemplate redisMQTemplate = new RedisMQTemplate(redisTemplate);
        // 添加拦截器
        interceptors.forEach(redisMQTemplate::addInterceptor);
        return redisMQTemplate;
    }

    /**
     * 创建 Redis Pub/Sub 广播消费的容器
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisMQTemplate redisMQTemplate, List<AbstractChannelMessageListener<?>> listeners) {
        // 创建 RedisMessageListenerContainer 对象
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        // 设置 RedisConnection 工厂
        container.setConnectionFactory(redisMQTemplate.getRedisTemplate().getRequiredConnectionFactory());
        // 添加监听器
        listeners.forEach(listener -> {
            listener.setRedisMQTemplate(redisMQTemplate);
            container.addMessageListener(listener, new ChannelTopic(listener.getChannel()));
            log.info("[redisMessageListenerContainer][注册 Channel({}) 对应的监听器({})]",
                    listener.getChannel(), listener.getClass().getName());
            
        });
        return container;
    }
    
}
