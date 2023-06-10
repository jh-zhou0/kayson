package cn.zjh.kayson.module.system.service.tenant;

import cn.hutool.core.collection.CollUtil;
import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.tenant.vo.packages.TenantPackageCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.tenant.vo.packages.TenantPackagePageReqVO;
import cn.zjh.kayson.module.system.controller.admin.tenant.vo.packages.TenantPackageUpdateReqVO;
import cn.zjh.kayson.module.system.convert.tenant.TenantPackageConvert;
import cn.zjh.kayson.module.system.dal.dataobject.tenant.TenantDO;
import cn.zjh.kayson.module.system.dal.dataobject.tenant.TenantPackageDO;
import cn.zjh.kayson.module.system.dal.mysql.tenant.TenantPackageMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.zjh.kayson.framework.common.exception.util.ServiceExceptionUtils.exception;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;

/**
 * 租户套餐 Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
@Validated
public class TenantPackageServiceImpl implements TenantPackageService {

    @Resource
    private TenantPackageMapper tenantPackageMapper;

    @Resource
    @Lazy // 避免循环依赖的报错
    private TenantService tenantService;

    @Override
    public Long createTenantPackage(TenantPackageCreateReqVO createReqVO) {
        // 插入
        TenantPackageDO tenantPackage = TenantPackageConvert.INSTANCE.convert(createReqVO);
        tenantPackageMapper.insert(tenantPackage);
        // 返回
        return tenantPackage.getId();
    }

    @Override
    public void updateTenantPackage(TenantPackageUpdateReqVO updateReqVO) {
        // 校验存在
        TenantPackageDO tenantPackage = validateTenantPackageExists(updateReqVO.getId());
        // 更新
        TenantPackageDO updateObj = TenantPackageConvert.INSTANCE.convert(updateReqVO);
        tenantPackageMapper.updateById(updateObj);
        
        // 如果菜单发生变化，则修改每个租户的菜单
        if (!CollUtil.isEqualList(tenantPackage.getMenuIds(), updateReqVO.getMenuIds())) {
            List<TenantDO> tenants = tenantService.getTenantListByPackageId(tenantPackage.getId());
            tenants.forEach(tenant -> tenantService.updateTenantRoleMenu(tenant.getId(), updateObj.getMenuIds()));
        }
    }

    private TenantPackageDO validateTenantPackageExists(Long id) {
        TenantPackageDO tenantPackage = tenantPackageMapper.selectById(id);
        if (tenantPackage == null) {
            throw exception(TENANT_PACKAGE_NOT_EXISTS);
        }
        return tenantPackage;
    }

    @Override
    public void deleteTenantPackage(Long id) {
        // 校验存在
        validateTenantPackageExists(id);
        // 校验正在使用
        validateTenantUsed(id);
        // 删除
        tenantPackageMapper.selectById(id);
    }

    private void validateTenantUsed(Long id) {
        if (tenantService.getTenantCountByPackageId(id) > 0) {
            throw exception(TENANT_PACKAGE_USED);
        }
    }

    @Override
    public TenantPackageDO getTenantPackage(Long id) {
        return tenantPackageMapper.selectById(id);
    }

    @Override
    public PageResult<TenantPackageDO> getTenantPackagePage(TenantPackagePageReqVO pageReqVO) {
        return tenantPackageMapper.selectPage(pageReqVO);
    }

    @Override
    public TenantPackageDO validateTenantPackage(Long id) {
        TenantPackageDO tenantPackage = tenantPackageMapper.selectById(id);
        if (tenantPackage == null) {
            throw exception(TENANT_PACKAGE_NOT_EXISTS);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(tenantPackage.getStatus())) {
            throw exception(TENANT_PACKAGE_DISABLE, tenantPackage.getName());
        }
        return tenantPackage;
    }

    @Override
    public List<TenantPackageDO> getTenantPackageListByStatus(Integer status) {
        return tenantPackageMapper.selectListByStatus(status);
    }
    
}
