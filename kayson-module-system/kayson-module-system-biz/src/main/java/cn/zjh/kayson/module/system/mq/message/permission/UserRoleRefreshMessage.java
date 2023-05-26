package cn.zjh.kayson.module.system.mq.message.permission;

import cn.zjh.kayson.framework.mq.core.pubsub.AbstractChannelMessage;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 用户与角色的数据刷新 Message
 * 
 * @author zjh - kayson
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public class UserRoleRefreshMessage extends AbstractChannelMessage {
    
    @Override
    public String getChannel() {
        return "system.user-role.refresh";
    }
    
}
