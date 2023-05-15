package cn.zjh.kayson.module.system.controller.admin.auth;

import cn.hutool.core.util.StrUtil;
import cn.zjh.kayson.framework.common.pojo.CommonResult;
import cn.zjh.kayson.framework.security.config.SecurityProperties;
import cn.zjh.kayson.framework.security.core.util.SecurityFrameworkUtils;
import cn.zjh.kayson.module.system.controller.admin.auth.vo.AuthLoginReqVO;
import cn.zjh.kayson.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import cn.zjh.kayson.module.system.controller.admin.auth.vo.AuthMenuRespVO;
import cn.zjh.kayson.module.system.controller.admin.auth.vo.AuthPermissionInfoRespVO;
import cn.zjh.kayson.module.system.convert.auth.AuthConvert;
import cn.zjh.kayson.module.system.dal.dataobject.permission.MenuDO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleDO;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import cn.zjh.kayson.module.system.enums.logger.LoginLogTypeEnum;
import cn.zjh.kayson.module.system.service.auth.AdminAuthService;
import cn.zjh.kayson.module.system.service.permission.PermissionService;
import cn.zjh.kayson.module.system.service.permission.RoleService;
import cn.zjh.kayson.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;

import static cn.zjh.kayson.framework.common.pojo.CommonResult.success;

/**
 * @author zjh - kayson
 */
@Tag(name = "管理后台 - 认证")
@RestController
@RequestMapping("/system/auth")
@Validated
public class AuthController {
    
    @Resource
    private AdminAuthService authService;
    
    @Resource
    private AdminUserService adminUserService;
    
    @Resource
    private PermissionService permissionService;
    
    @Resource
    private RoleService roleService;
    
    @Resource
    private SecurityProperties securityProperties;

    @PostMapping("/login")
    @PermitAll
    @Operation(summary = "使用账号密码登录")
    public CommonResult<AuthLoginRespVO> login(@RequestBody @Valid AuthLoginReqVO reqVO) {
        return success(authService.login(reqVO));
    }
    
    @PostMapping("/logout")
    @PermitAll
    @Operation(summary = "登出系统")
    public CommonResult<Boolean> logout(HttpServletRequest request) {
        String token = SecurityFrameworkUtils.obtainAuthorization(request, securityProperties.getTokenHeader());
        if (StrUtil.isNotBlank(token)) {
            authService.logout(token, LoginLogTypeEnum.LOGOUT_SELF.getType());
        }
        return success(true);
    }

    @PostMapping("/refresh-token")
    @PermitAll
    @Operation(summary = "刷新令牌")
    @Parameter(name = "refreshToken", description = "刷新令牌", required = true)
    public CommonResult<AuthLoginRespVO> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        return success(authService.refreshToken(refreshToken));
    }

    @GetMapping("/get-permission-info")
    @Operation(summary = "获取登录用户的权限信息")
    public CommonResult<AuthPermissionInfoRespVO> getPermissionInfo() {
        // 获得用户信息
        AdminUserDO user = adminUserService.getUser(SecurityFrameworkUtils.getLoginUserId());
        if (user == null) {
            return null;
        }
        // 获得角色列表
        Set<Long> roleIds = permissionService.getUserRoleIds(user.getId());
        List<RoleDO> roleList = roleService.getRoleList(roleIds);
        // 获得菜单列表
        List<MenuDO> menuList = permissionService.getRoleMenuList(roleList);
        // 拼接结果返回
        return success(AuthConvert.INSTANCE.convert(user, roleList, menuList));
    }

    @GetMapping("/list-menus")
    @Operation(summary = "获得登录用户的菜单列表")
    public CommonResult<List<AuthMenuRespVO>> getMenuList() {
        // 获得角色列表
        Set<Long> roleIds = permissionService.getUserRoleIds(SecurityFrameworkUtils.getLoginUserId());
        List<RoleDO> roleList = roleService.getRoleList(roleIds);
        // 获得菜单列表
        List<MenuDO> menuList = permissionService.getRoleMenuList(roleList);
        // 转换成 Tree 结构返回
        return success(AuthConvert.INSTANCE.buildMenuTree(menuList));
    }
    
}
