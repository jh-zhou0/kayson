package cn.zjh.kayson.framework.mq.core.interceptor;

import cn.zjh.kayson.framework.mq.core.message.AbstractRedisMessage;

/**
 * @author zjh - kayson
 */
public interface RedisMessageInterceptor {

    default void sendMessageBefore(AbstractRedisMessage message) {
    }

    default void sendMessageAfter(AbstractRedisMessage message) {
    }

    default void consumeMessageBefore(AbstractRedisMessage message) {
    }

    default void consumeMessageAfter(AbstractRedisMessage message) {
    }
    
}
