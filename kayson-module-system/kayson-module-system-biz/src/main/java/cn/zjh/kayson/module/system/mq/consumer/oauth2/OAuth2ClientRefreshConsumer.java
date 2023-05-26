package cn.zjh.kayson.module.system.mq.consumer.oauth2;

import cn.zjh.kayson.framework.mq.core.pubsub.AbstractChannelMessageListener;
import cn.zjh.kayson.module.system.mq.message.oauth2.OAuth2ClientRefreshMessage;
import cn.zjh.kayson.module.system.service.oauth2.OAuth2ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link OAuth2ClientRefreshMessage} 的消费者
 * 
 * @author zjh - kayson
 */
@Component
@Slf4j
public class OAuth2ClientRefreshConsumer extends AbstractChannelMessageListener<OAuth2ClientRefreshMessage> {
    
    @Resource
    private OAuth2ClientService oauth2ClientService;
    
    @Override
    public void onMessage(OAuth2ClientRefreshMessage message) {
        log.info("[onMessage][收到 OAuth2Client 刷新消息]");
        oauth2ClientService.initLocalCache();
    }
    
}
