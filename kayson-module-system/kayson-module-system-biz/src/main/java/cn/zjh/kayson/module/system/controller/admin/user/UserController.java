package cn.zjh.kayson.module.system.controller.admin.user;

import cn.hutool.core.collection.CollUtil;
import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.pojo.CommonResult;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.user.vo.user.*;
import cn.zjh.kayson.module.system.convert.user.UserConvert;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import cn.zjh.kayson.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static cn.zjh.kayson.framework.common.pojo.CommonResult.success;

/**
 * @author zjh - kayson
 */
@Tag(name = "管理后台 - 用户")
@RestController
@RequestMapping("/system/user")
@Validated
public class UserController {

    @Resource
    private AdminUserService adminUserService;

    @PostMapping("/create")
    @Operation(summary = "新增用户")
    public CommonResult<Long> createUser(@Valid @RequestBody UserCreateReqVO reqVO) {
        Long userId = adminUserService.createUser(reqVO);
        return success(userId);
    }

    @PutMapping("/update")
    @Operation(summary = "修改用户")
    public CommonResult<Boolean> updateUser(@Valid @RequestBody UserUpdateReqVO reqVO) {
        adminUserService.updateUser(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<Boolean> deleteUser(@RequestParam("id") Long id) {
        adminUserService.deleteUser(id);
        return success(true);
    }
    
    @GetMapping("/get")
    @Operation(summary = "获得用户详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<UserRespVO> getUser(@RequestParam("id") Long id) {
        AdminUserDO user = adminUserService.getUser(id);
        // TODO: 获得部门数据，设置到 UserPageItemRespVO 中
        return success(UserConvert.INSTANCE.convert(user).setDept(null));
    }
    
    @GetMapping("/page")
    @Operation(summary = "获得用户分页列表")
    public CommonResult<PageResult<UserPageItemRespVO>> getUserPage(@Valid UserPageReqVO reqVO) {
        // 获得用户分页列表
        PageResult<AdminUserDO> pageResult = adminUserService.getUserPage(reqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal())); // 返回空
        }
        
        // TODO: 获取部门数据
        List<UserPageItemRespVO> userList = new ArrayList<>(pageResult.getList().size());
        pageResult.getList().forEach(user -> {
            UserPageItemRespVO respVO = UserConvert.INSTANCE.convert(user);
            // TODO：拼接部门数据
            userList.add(respVO);
        });
        return success(new PageResult<>(userList, pageResult.getTotal()));
    }

    @PutMapping("/update-password")
    @Operation(summary = "重置用户密码")
    public CommonResult<Boolean> updateUserPassword(@Valid @RequestBody UserUpdatePasswordReqVO reqVO) {
        adminUserService.updateUserPassword(reqVO.getId(), reqVO.getPassword());
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "修改用户状态")
    public CommonResult<Boolean> updateUserStatus(@Valid @RequestBody UserUpdateStatusReqVO reqVO) {
        adminUserService.updateUserStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取用户精简信息列表", description = "只包含被开启的用户，主要用于前端的下拉选项")
    public CommonResult<List<UserSimpleRespVO>> getSimpleUserList() {
        // 获用户列表，只要开启状态的
        List<AdminUserDO> list = adminUserService.getUserListByStatus(CommonStatusEnum.ENABLE.getValue());
        // 排序后，返回给前端
        return success(UserConvert.INSTANCE.convertList04(list));
    }
}
