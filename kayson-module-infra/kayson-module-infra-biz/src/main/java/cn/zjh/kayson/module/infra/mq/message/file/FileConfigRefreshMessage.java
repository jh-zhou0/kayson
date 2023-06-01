package cn.zjh.kayson.module.infra.mq.message.file;

import cn.zjh.kayson.framework.mq.core.pubsub.AbstractChannelMessage;
import lombok.Data;

/**
 * 文件配置数据刷新 Message
 * 
 * @author zjh - kayson
 */
@Data
public class FileConfigRefreshMessage extends AbstractChannelMessage {

    @Override
    public String getChannel() {
        return "infra.file-config.refresh";
    }

}
