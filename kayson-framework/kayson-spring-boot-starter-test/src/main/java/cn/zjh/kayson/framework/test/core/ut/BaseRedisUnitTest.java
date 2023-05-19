package cn.zjh.kayson.framework.test.core.ut;

import cn.zjh.kayson.framework.test.config.RedisTestConfiguration;
import cn.zjh.kayson.framework.redis.config.KaysonRedisAutoConfiguration;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * 依赖内存 Redis 的单元测试
 * 
 * @author zjh - kayson
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = BaseRedisUnitTest.Application.class)
@ActiveProfiles("unit-test") // 设置使用 application-unit-test 配置文件
public class BaseRedisUnitTest {

    @Import({
            // Redis 配置类
            RedisTestConfiguration.class, // Redis 测试配置类，用于启动 RedisServer
            RedisAutoConfiguration.class, // Spring Redis 自动配置类
            KaysonRedisAutoConfiguration.class, // 自己的 Redis 自动配置类
            RedissonAutoConfiguration.class // Redisson 自动配置类
    })
    public static class Application {
    }
    
}
