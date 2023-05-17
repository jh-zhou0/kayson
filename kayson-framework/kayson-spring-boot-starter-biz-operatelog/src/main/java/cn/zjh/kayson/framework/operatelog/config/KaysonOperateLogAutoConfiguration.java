package cn.zjh.kayson.framework.operatelog.config;

import cn.zjh.kayson.framework.operatelog.core.aop.OperateLogAspect;
import cn.zjh.kayson.framework.operatelog.core.service.OperateLogFrameworkService;
import cn.zjh.kayson.framework.operatelog.core.service.OperateLogFrameworkServiceImpl;
import cn.zjh.kayson.module.system.api.logger.OperateLogApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author zjh - kayson
 */
@AutoConfiguration
public class KaysonOperateLogAutoConfiguration {
    
    @Bean
    public OperateLogAspect operateLogAspect() {
        return new OperateLogAspect();
    }

    @Bean
    public OperateLogFrameworkService operateLogFrameworkService(OperateLogApi operateLogApi) {
        return new OperateLogFrameworkServiceImpl(operateLogApi);
    }
    
}
