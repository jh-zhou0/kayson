package cn.zjh.kayson.module.system.mq.producer.permission;

import cn.zjh.kayson.framework.mq.core.RedisMQTemplate;
import cn.zjh.kayson.module.system.mq.message.permission.MenuRefreshMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Menu 菜单相关消息的 Producer
 * 
 * @author zjh - kayson
 */
@Component
public class MenuProducer {
    
    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link MenuRefreshMessage} 消息
     */
    public void sendMenuRefreshMessage() {
        MenuRefreshMessage message = new MenuRefreshMessage();
        redisMQTemplate.send(message);
    }
    
}
