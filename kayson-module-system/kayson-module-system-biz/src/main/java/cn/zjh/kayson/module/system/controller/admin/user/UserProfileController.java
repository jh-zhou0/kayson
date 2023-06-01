package cn.zjh.kayson.module.system.controller.admin.user;

import cn.hutool.core.collection.CollUtil;
import cn.zjh.kayson.framework.common.pojo.CommonResult;
import cn.zjh.kayson.module.system.controller.admin.user.vo.profile.UserProfileRespVO;
import cn.zjh.kayson.module.system.controller.admin.user.vo.profile.UserProfileUpdatePasswordReqVO;
import cn.zjh.kayson.module.system.controller.admin.user.vo.profile.UserProfileUpdateReqVO;
import cn.zjh.kayson.module.system.convert.user.UserConvert;
import cn.zjh.kayson.module.system.dal.dataobject.dept.DeptDO;
import cn.zjh.kayson.module.system.dal.dataobject.dept.PostDO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleDO;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import cn.zjh.kayson.module.system.service.dept.DeptService;
import cn.zjh.kayson.module.system.service.dept.PostService;
import cn.zjh.kayson.module.system.service.permission.PermissionService;
import cn.zjh.kayson.module.system.service.permission.RoleService;
import cn.zjh.kayson.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;

import static cn.zjh.kayson.framework.common.exception.util.ServiceExceptionUtils.exception;
import static cn.zjh.kayson.framework.common.pojo.CommonResult.success;
import static cn.zjh.kayson.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.FILE_IS_EMPTY;

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
    
    @Resource
    private PermissionService permissionService;
    
    @Resource
    private RoleService roleService;
    
    @Resource
    private DeptService deptService;
    
    @Resource
    private PostService postService;

    @GetMapping("/get")
    @Operation(summary = "获得登录用户信息")
    public CommonResult<UserProfileRespVO> profile() {
        // 获得用户基本信息
        AdminUserDO user = adminUserService.getUser(getLoginUserId());
        UserProfileRespVO respVO = UserConvert.INSTANCE.convert02(user);
        // 获得用户角色
        Set<Long> roleIds = permissionService.getUserRoleIds(user.getId());
        List<RoleDO> roleList = roleService.getRoleList(roleIds);
        List<UserProfileRespVO.Role> roles = UserConvert.INSTANCE.convertList02(roleList);
        respVO.setRoles(roles);
        // 获得部门信息
        if (user.getDeptId() != null) {
            DeptDO deptDO = deptService.getDept(user.getDeptId());
            UserProfileRespVO.Dept dept = UserConvert.INSTANCE.convert02(deptDO);
            respVO.setDept(dept);
        }
        // 获得岗位信息
        if (CollUtil.isNotEmpty(user.getPostIds())) {
            List<PostDO> postList = postService.getPostList(user.getPostIds());
            respVO.setPosts(UserConvert.INSTANCE.convertList03(postList));
        }
        return success(respVO);
    }

    @PutMapping("/update")
    @Operation(summary = "修改用户个人信息")
    public CommonResult<Boolean> updateUserProfile(@Valid @RequestBody UserProfileUpdateReqVO reqVO) {
        adminUserService.updateUserProfile(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/update-password")
    @Operation(summary = "修改用户个人密码")
    public CommonResult<Boolean> updateUserProfilePassword(@Valid @RequestBody UserProfileUpdatePasswordReqVO reqVO) {
        adminUserService.updateUserPassword(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping(value = "/update-avatar")
    @Operation(summary = "上传用户个人头像")
    public CommonResult<String> updateUserAvatar(@RequestParam("avatarFile") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw exception(FILE_IS_EMPTY);
        }
        String avatar = adminUserService.updateUserAvatar(getLoginUserId(), file.getInputStream());
        return success(avatar);
    }
}
