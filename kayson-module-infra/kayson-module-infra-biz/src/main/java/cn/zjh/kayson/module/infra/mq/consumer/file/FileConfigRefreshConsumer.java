package cn.zjh.kayson.module.infra.mq.consumer.file;

import cn.zjh.kayson.framework.mq.core.pubsub.AbstractChannelMessageListener;
import cn.zjh.kayson.module.infra.mq.message.file.FileConfigRefreshMessage;
import cn.zjh.kayson.module.infra.service.file.FileConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link FileConfigRefreshMessage} 的消费者
 *
 * @author zjh - kayson
 */
@Component
@Slf4j
public class FileConfigRefreshConsumer extends AbstractChannelMessageListener<FileConfigRefreshMessage> {

    @Resource
    private FileConfigService fileConfigService;

    @Override
    public void onMessage(FileConfigRefreshMessage message) {
        log.info("[onMessage][收到 FileConfig 刷新消息]");
        fileConfigService.initLocalCache();
    }

}
