package cn.zjh.kayson.module.system.controller.admin.user;

import cn.zjh.kayson.framework.common.pojo.CommonResult;
import cn.zjh.kayson.module.system.controller.admin.user.vo.profile.UserProfileRespVO;
import cn.zjh.kayson.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.zjh.kayson.framework.common.pojo.CommonResult.success;

/**
 * @author zjh - kayson
 */
@Tag(name = "管理后台 - 用户个人中心")
@RestController
@RequestMapping("/system/user/profile")
@Validated
public class UserProfileController {
    
    @Resource
    private AdminUserService adminUserService;

    @GetMapping("/get")
    @Operation(summary = "获得登录用户信息")
    public CommonResult<UserProfileRespVO> profile() {
        // TODO: 获得用户基本信息，角色，部门，岗位，社交用户组
        return success(new UserProfileRespVO());
    }
}
