package cn.zjh.kayson.module.system.convert.tenant;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.tenant.vo.packages.TenantPackageCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.tenant.vo.packages.TenantPackageRespVO;
import cn.zjh.kayson.module.system.controller.admin.tenant.vo.packages.TenantPackageSimpleRespVO;
import cn.zjh.kayson.module.system.controller.admin.tenant.vo.packages.TenantPackageUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.tenant.TenantPackageDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface TenantPackageConvert {

    TenantPackageConvert INSTANCE  = Mappers.getMapper(TenantPackageConvert.class);

    TenantPackageDO convert(TenantPackageCreateReqVO bean);

    TenantPackageDO convert(TenantPackageUpdateReqVO bean);

    TenantPackageRespVO convert(TenantPackageDO bean);

    PageResult<TenantPackageRespVO> convertPage(PageResult<TenantPackageDO> page);

    List<TenantPackageSimpleRespVO> convertList(List<TenantPackageDO> list);
    
}
