package cn.zjh.kayson.framework.quartz.config;

import cn.zjh.kayson.framework.quartz.core.schedler.SchedulerManager;
import org.quartz.Scheduler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务 Configuration
 * 
 * @author zjh - kayson
 */
@AutoConfiguration
@EnableScheduling // 开启 Spring 自带的定时任务
public class KaysonQuartzAutoConfiguration {

    @Bean
    public SchedulerManager schedulerManager(Scheduler scheduler) {
        return new SchedulerManager(scheduler);
    }
    
}
