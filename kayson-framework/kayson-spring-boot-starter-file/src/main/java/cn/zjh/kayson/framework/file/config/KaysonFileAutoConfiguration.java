package cn.zjh.kayson.framework.file.config;

import cn.zjh.kayson.framework.file.core.client.FileClientFactory;
import cn.zjh.kayson.framework.file.core.client.FileClientFactoryImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 文件配置类
 * 
 * @author zjh - kayson
 */
@AutoConfiguration
public class KaysonFileAutoConfiguration {
    
    @Bean
    public FileClientFactory fileClientFactory() {
        return new FileClientFactoryImpl();
    }
    
}
