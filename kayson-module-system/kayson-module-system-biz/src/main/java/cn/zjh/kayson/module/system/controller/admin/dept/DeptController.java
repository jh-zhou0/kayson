package cn.zjh.kayson.module.system.controller.admin.dept;

import cn.zjh.kayson.framework.common.pojo.CommonResult;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.DeptCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.DeptListReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.DeptRespVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.DeptUpdateReqVO;
import cn.zjh.kayson.module.system.convert.dept.DeptConvert;
import cn.zjh.kayson.module.system.dal.dataobject.dept.DeptDO;
import cn.zjh.kayson.module.system.service.dept.DeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;

import static cn.zjh.kayson.framework.common.pojo.CommonResult.success;

/**
 * @author zjh - kayson
 */
@Tag(name = "管理后台 - 部门")
@RestController
@RequestMapping("/system/dept")
@Validated
public class DeptController {
    
    @Resource
    private DeptService deptService;

    @PostMapping("create")
    @Operation(summary = "创建部门")
    public CommonResult<Long> createDept(@Valid @RequestBody DeptCreateReqVO reqVO) {
        Long deptId = deptService.createDept(reqVO);
        return success(deptId);
    }

    @PutMapping("update")
    @Operation(summary = "更新部门")
    public CommonResult<Boolean> updateDept(@Valid @RequestBody DeptUpdateReqVO reqVO) {
        deptService.updateDept(reqVO);
        return success(true);
    }

    @DeleteMapping("delete")
    @Operation(summary = "删除部门")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<Boolean> deleteDept(@RequestParam("id") Long id) {
        deptService.deleteDept(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得部门信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<DeptRespVO> getDept(@RequestParam("id") Long id) {
        DeptDO dept = deptService.getDept(id);
        return success(DeptConvert.INSTANCE.convert(dept));
    }

    @GetMapping("/list")
    @Operation(summary = "获取部门列表")
    public CommonResult<List<DeptRespVO>> getDeptList(DeptListReqVO reqVO) {
        List<DeptDO> list = deptService.getDeptList(reqVO);
        list.sort(Comparator.comparing(DeptDO::getSort));
        return success(DeptConvert.INSTANCE.convertList(list));
    }
}
