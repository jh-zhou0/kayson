package cn.zjh.kayson.module.system.convert.permission;

import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.*;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface RoleConvert {

    RoleConvert INSTANCE = Mappers.getMapper(RoleConvert.class);
    
    RoleDO convert(RoleCreateReqVO bean);
    
    RoleDO convert(RoleUpdateReqVO bean);

    RoleRespVO convert(RoleDO bean);

    List<RoleSimpleRespVO> convertList(List<RoleDO> list);

    List<RoleExcelVO> convertList01(List<RoleDO> list);
}
