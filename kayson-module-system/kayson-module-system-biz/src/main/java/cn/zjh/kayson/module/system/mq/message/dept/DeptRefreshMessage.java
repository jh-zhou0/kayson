package cn.zjh.kayson.module.system.mq.message.dept;

import cn.zjh.kayson.framework.mq.core.pubsub.AbstractChannelMessage;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 部门数据刷新 Message
 * 
 * @author zjh - kayson
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public class DeptRefreshMessage extends AbstractChannelMessage {

    @Override
    public String getChannel() {
        return "system.dept.refresh";
    }
    
}
