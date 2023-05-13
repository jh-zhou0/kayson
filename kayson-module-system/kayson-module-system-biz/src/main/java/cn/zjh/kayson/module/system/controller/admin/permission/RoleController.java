package cn.zjh.kayson.module.system.controller.admin.permission;

import cn.zjh.kayson.framework.common.pojo.CommonResult;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RoleCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RoleRespVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RoleUpdateReqVO;
import cn.zjh.kayson.module.system.convert.permission.RoleConvert;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleDO;
import cn.zjh.kayson.module.system.service.permission.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.zjh.kayson.framework.common.pojo.CommonResult.success;

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
    public CommonResult<Long> createRole(@Valid @RequestBody RoleCreateReqVO reqVO) {
        return success(roleService.createRole(reqVO, null));
    }

    @PutMapping("/update")
    @Operation(summary = "修改角色")
    public CommonResult<Boolean> updateRole(@Valid @RequestBody RoleUpdateReqVO reqVO) {
        roleService.updateRole(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除角色")
    @Parameter(name = "id", description = "角色编号", required = true, example = "1024")
    public CommonResult<Boolean> deleteRole(@RequestParam("id") Long id) {
        roleService.deleteRole(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得角色信息")
    public CommonResult<RoleRespVO> getRole(@RequestParam("id") Long id) {
        RoleDO role = roleService.getRole(id);
        return success(RoleConvert.INSTANCE.convert(role));
    }

    @GetMapping("/page")
    @Operation(summary = "获得角色分页")
    public CommonResult<PageResult<RoleDO>> getRolePage(@Valid RolePageReqVO reqVO) {
        return success(roleService.getRolePage(reqVO));
    }
}
