package cn.zjh.kayson.framework.tenant.core.mq;

import cn.hutool.core.util.StrUtil;
import cn.zjh.kayson.framework.mq.core.interceptor.RedisMessageInterceptor;
import cn.zjh.kayson.framework.mq.core.message.AbstractRedisMessage;
import cn.zjh.kayson.framework.tenant.core.context.TenantContextHolder;

import static cn.zjh.kayson.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;

/**
 * 多租户 {@link AbstractRedisMessage} 拦截器
 *
 * 1. Producer 发送消息时，将 {@link TenantContextHolder} 租户编号，添加到消息的 Header 中
 * 2. Consumer 消费消息时，将消息的 Header 的租户编号，添加到 {@link TenantContextHolder} 中
 * 
 * @author zjh - kayson
 */
public class TenantRedisMessageInterceptor implements RedisMessageInterceptor {

    @Override
    public void sendMessageBefore(AbstractRedisMessage message) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            message.setHeader(HEADER_TENANT_ID, tenantId.toString());
        }
    }

    @Override
    public void consumeMessageBefore(AbstractRedisMessage message) {
        String tenantId = message.getHeader(HEADER_TENANT_ID);
        if (StrUtil.isNotEmpty(tenantId)) {
            TenantContextHolder.setTenantId(Long.valueOf(tenantId));
        }
    }

    @Override
    public void consumeMessageAfter(AbstractRedisMessage message) {
        // 注意，Consumer 是一个逻辑的入口，所以不考虑原本上下文就存在租户编号的情况
        TenantContextHolder.clear();
    }
}
