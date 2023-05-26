package cn.zjh.kayson.module.system.mq.producer.permission;

import cn.zjh.kayson.framework.mq.core.RedisMQTemplate;
import cn.zjh.kayson.module.system.mq.message.permission.RoleMenuRefreshMessage;
import cn.zjh.kayson.module.system.mq.message.permission.UserRoleRefreshMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Permission 权限相关消息的 Producer
 * 
 * @author zjh - kayson
 */
@Component
public class PermissionProducer {
    
    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link RoleMenuRefreshMessage} 消息
     */
    public void sendRoleMenuRefreshMessage() {
        RoleMenuRefreshMessage message = new RoleMenuRefreshMessage();
        redisMQTemplate.send(message);
    }

    /**
     * 发送 {@link UserRoleRefreshMessage} 消息
     */
    public void sendUserRoleRefreshMessage() {
        UserRoleRefreshMessage message = new UserRoleRefreshMessage();
        redisMQTemplate.send(message);
    }
    
}
