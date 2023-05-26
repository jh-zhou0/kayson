package cn.zjh.kayson.module.system.mq.message.permission;

import cn.zjh.kayson.framework.mq.core.pubsub.AbstractChannelMessage;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 角色数据刷新 Message
 * 
 * @author zjh - kayson
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public class RoleRefreshMessage extends AbstractChannelMessage {

    @Override
    public String getChannel() {
        return "system.role.refresh";
    }
    
}
