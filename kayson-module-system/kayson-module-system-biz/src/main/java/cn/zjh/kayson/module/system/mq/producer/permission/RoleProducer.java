package cn.zjh.kayson.module.system.mq.producer.permission;

import cn.zjh.kayson.framework.mq.core.RedisMQTemplate;
import cn.zjh.kayson.module.system.mq.message.permission.RoleRefreshMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Role 角色相关消息的 Producer
 * 
 * @author zjh - kayson
 */
@Component
public class RoleProducer {
    
    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link RoleRefreshMessage} 消息
     */
    public void sendRoleRefreshMessage() {
        RoleRefreshMessage message = new RoleRefreshMessage();
        redisMQTemplate.send(message);
    }
    
}
