package cn.zjh.kayson.module.system.mq.producer.dept;

import cn.zjh.kayson.framework.mq.core.RedisMQTemplate;
import cn.zjh.kayson.module.system.mq.message.dept.DeptRefreshMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Dept 部门相关消息的 Producer
 * 
 * @author zjh - kayson
 */
@Component
public class DeptProducer {
    
    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link DeptRefreshMessage} 消息
     */
    public void sendDeptRefreshMessage() {
        DeptRefreshMessage message = new DeptRefreshMessage();
        redisMQTemplate.send(message);
    }
}
