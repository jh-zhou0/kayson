package cn.zjh.kayson.module.system.controller.admin.dict;

import cn.zjh.kayson.framework.common.pojo.CommonResult;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.*;
import cn.zjh.kayson.module.system.convert.dict.DictDataConvert;
import cn.zjh.kayson.module.system.dal.dataobject.dict.DictDataDO;
import cn.zjh.kayson.module.system.service.dict.DictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.zjh.kayson.framework.common.pojo.CommonResult.success;

/**
 * @author zjh - kayson
 */
@Tag(name = "管理后台 - 字典数据")
@RestController
@RequestMapping("/system/dict-data")
@Validated
public class DictDataController {
    
    @Resource
    private DictDataService dictDataService;

    @PostMapping("/create")
    @Operation(summary = "新增字典数据")
    @PreAuthorize("@ss.hasPermission('system:dict:create')")
    public CommonResult<Long> createDictData(@Valid @RequestBody DictDataCreateReqVO reqVO) {
        Long dictDataId = dictDataService.createDictData(reqVO);
        return success(dictDataId);
    }

    @PutMapping("/update")
    @Operation(summary = "修改字典数据")
    @PreAuthorize("@ss.hasPermission('system:dict:update')")
    public CommonResult<Boolean> updateDictData(@Valid @RequestBody DictDataUpdateReqVO reqVO) {
        dictDataService.updateDictData(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除字典数据")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:dict:delete')")
    public CommonResult<Boolean> deleteDictData(Long id) {
        dictDataService.deleteDictData(id);
        return success(true);
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获得全部字典数据列表", description = "一般用于管理后台缓存字典数据在本地")
    // 无需添加权限认证，因为前端全局都需要
    public CommonResult<List<DictDataSimpleRespVO>> getSimpleDictDataList() {
        List<DictDataDO> list = dictDataService.getDictDataList();
        return success(DictDataConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "/获得字典类型的分页列表")
    @PreAuthorize("@ss.hasPermission('system:dict:query')")
    public CommonResult<PageResult<DictDataRespVO>> getDictTypePage(@Valid DictDataPageReqVO reqVO) {
        return success(DictDataConvert.INSTANCE.convertPage(dictDataService.getDictDataPage(reqVO)));
    }

    @GetMapping(value = "/get")
    @Operation(summary = "/查询字典数据详细")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:dict:query')")
    public CommonResult<DictDataRespVO> getDictData(@RequestParam("id") Long id) {
        return success(DictDataConvert.INSTANCE.convert(dictDataService.getDictData(id)));
    }
    
}