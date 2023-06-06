package cn.zjh.kayson.framework.lock4j.config;

import cn.zjh.kayson.framework.lock4j.core.DefaultLockFailureStrategy;
import com.baomidou.lock.spring.boot.autoconfigure.LockAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author zjh - kayson
 */
@AutoConfiguration(before = LockAutoConfiguration.class)
public class KaysonLock4jConfiguration {
    
    @Bean
    public DefaultLockFailureStrategy defaultLockFailureStrategy() {
        return new DefaultLockFailureStrategy();
    }
    
}
