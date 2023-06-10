package cn.zjh.kayson.module.system.convert.tenant;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.tenant.vo.tenant.TenantCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.tenant.vo.tenant.TenantExcelVO;
import cn.zjh.kayson.module.system.controller.admin.tenant.vo.tenant.TenantRespVO;
import cn.zjh.kayson.module.system.controller.admin.tenant.vo.tenant.TenantUpdateReqVO;
import cn.zjh.kayson.module.system.controller.admin.user.vo.user.UserCreateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.tenant.TenantDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface TenantConvert {

    TenantConvert INSTANCE = Mappers.getMapper(TenantConvert.class);

    TenantDO convert(TenantCreateReqVO bean);

    TenantRespVO convert(TenantDO bean);

    PageResult<TenantRespVO> convertPage(PageResult<TenantDO> page);

    List<TenantExcelVO> convertList02(List<TenantDO> list);

    default UserCreateReqVO convert02(TenantCreateReqVO createReqVO) {
        UserCreateReqVO reqVO = new UserCreateReqVO();
        reqVO.setUsername(createReqVO.getUsername());
        reqVO.setPassword(createReqVO.getPassword());
        reqVO.setNickname(createReqVO.getContactName());
        reqVO.setMobile(createReqVO.getContactMobile());
        return reqVO;
    }

    TenantDO convert(TenantUpdateReqVO bean);
    
}
