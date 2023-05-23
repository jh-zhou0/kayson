package cn.zjh.kayson.module.system.controller.admin.permission;

import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.pojo.CommonResult;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.*;
import cn.zjh.kayson.module.system.convert.permission.RoleConvert;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleDO;
import cn.zjh.kayson.module.system.service.permission.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;

import static cn.zjh.kayson.framework.common.pojo.CommonResult.success;
import static java.util.Collections.singleton;

/**
 * @author zjh - kayson
 */
@Tag(name = "管理后台 - 角色")
@RestController
@RequestMapping("/system/role")
@Validated
public class RoleController {
    
    @Resource
    private RoleService roleService;

    @PostMapping("/create")
    @Operation(summary = "创建角色")
    @PreAuthorize("@ss.hasPermission('system:role:create')")
    public CommonResult<Long> createRole(@Valid @RequestBody RoleCreateReqVO reqVO) {
        return success(roleService.createRole(reqVO, null));
    }

    @PutMapping("/update")
    @Operation(summary = "修改角色")
    @PreAuthorize("@ss.hasPermission('system:role:update')")
    public CommonResult<Boolean> updateRole(@Valid @RequestBody RoleUpdateReqVO reqVO) {
        roleService.updateRole(reqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "修改角色状态")
    @PreAuthorize("@ss.hasPermission('system:role:update')")
    public CommonResult<Boolean> updateRoleStatus(@Valid @RequestBody RoleUpdateStatusReqVO reqVO) {
        roleService.updateRoleStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除角色")
    @Parameter(name = "id", description = "角色编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:role:delete')")
    public CommonResult<Boolean> deleteRole(@RequestParam("id") Long id) {
        roleService.deleteRole(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得角色信息")
    @PreAuthorize("@ss.hasPermission('system:role:query')")
    public CommonResult<RoleRespVO> getRole(@RequestParam("id") Long id) {
        RoleDO role = roleService.getRole(id);
        return success(RoleConvert.INSTANCE.convert(role));
    }

    @GetMapping("/page")
    @Operation(summary = "获得角色分页")
    @PreAuthorize("@ss.hasPermission('system:role:query')")
    public CommonResult<PageResult<RoleDO>> getRolePage(@Valid RolePageReqVO reqVO) {
        return success(roleService.getRolePage(reqVO));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取角色精简信息列表", description = "只包含被开启的角色，主要用于前端的下拉选项")
    public CommonResult<List<RoleSimpleRespVO>> getSimpleRoleList() {
        // 获得角色列表，只要开启状态的
        List<RoleDO> list = roleService.getRoleListByStatus(singleton(CommonStatusEnum.ENABLE.getStatus()));
        // 排序后，返回给前端
        list.sort(Comparator.comparing(RoleDO::getSort));
        return success(RoleConvert.INSTANCE.convertList(list));
    }
}
