package cn.zjh.kayson.module.infra.controller.admin.test;

import cn.zjh.kayson.framework.common.pojo.CommonResult;
import cn.zjh.kayson.module.infra.dal.dataobject.test.TestDemoDO;
import cn.zjh.kayson.module.infra.service.test.TestDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.util.List;

import static cn.zjh.kayson.framework.common.pojo.CommonResult.success;

/**
 * @author zjh - kayson
 */
@Tag(name = "管理后台 - 测试 mapper.xml")
@RestController
@RequestMapping("/infra/test-demo")
public class TestDemoController {
    
    @Resource
    private TestDemoService testDemoService;

    @GetMapping("/get")
    @Operation(summary = "获得 test 列表")
    @PermitAll
    public CommonResult<List<TestDemoDO>> getTestDemo() {
        return success(testDemoService.getList());
    }
    
}
