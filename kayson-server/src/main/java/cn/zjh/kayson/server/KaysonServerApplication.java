package cn.zjh.kayson.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动类
 *
 * @author zjh - kayson
 */
@SpringBootApplication(scanBasePackages = {"cn.zjh.kayson.server", "cn.zjh.kayson.module"})
public class KaysonServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KaysonServerApplication.class, args);
    }
}
