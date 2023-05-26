package cn.zjh.kayson.module.system.mq.message.oauth2;

import cn.zjh.kayson.framework.mq.core.pubsub.AbstractChannelMessage;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OAuth 2.0 客户端的数据刷新 Message
 * 
 * @author zjh - kayson
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public class OAuth2ClientRefreshMessage extends AbstractChannelMessage {
    
    @Override
    public String getChannel() {
        return "system.oauth2-client.refresh";
    }
    
}
