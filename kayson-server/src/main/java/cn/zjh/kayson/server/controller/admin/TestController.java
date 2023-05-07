package cn.zjh.kayson.server.controller.admin;

import cn.zjh.kayson.framework.common.pojo.CommonResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zjh - kayson
 */
@RestController
@RequestMapping("/test")
public class TestController {
    
    @GetMapping("/get")
    public CommonResult<String> get() {
        return CommonResult.success("test get success");
    }
    
    @GetMapping("/exception")
    public CommonResult<String> exception() {
        throw new RuntimeException("test exception");
    }
}
