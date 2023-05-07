package cn.zjh.kayson.server.controller.admin;

import cn.zjh.kayson.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zjh - kayson
 */
@Tag(name = "Test 接口")
@RestController
@RequestMapping("/test")
public class TestController {
    
    @Operation(summary = "测试get方法")
    @GetMapping("/get")
    public CommonResult<String> get() {
        return CommonResult.success("test get success");
    }
    
    @Operation(summary = "测试exception方法")
    @GetMapping("/exception")
    public CommonResult<String> exception() {
        throw new RuntimeException("test exception");
    }
}
