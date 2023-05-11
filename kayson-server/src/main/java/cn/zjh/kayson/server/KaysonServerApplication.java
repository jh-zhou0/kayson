package cn.zjh.kayson.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动类
 *
 * @author zjh - kayson
 */
@SuppressWarnings("SpringComponentScan") // 忽略 IDEA 无法识别 ${kayson.info.base-package}
@SpringBootApplication(scanBasePackages = {"${kayson.info.base-package}.server", "${kayson.info.base-package}.module"})
public class KaysonServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KaysonServerApplication.class, args);
    }
}
