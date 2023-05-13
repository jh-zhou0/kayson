package cn.zjh.kayson.module.system.convert.permission;

import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RoleCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RoleRespVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RoleUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author zjh - kayson
 */
@Mapper
public interface RoleConvert {

    RoleConvert INSTANCE = Mappers.getMapper(RoleConvert.class);
    
    RoleDO convert(RoleCreateReqVO bean);
    
    RoleDO convert(RoleUpdateReqVO bean);

    RoleRespVO convert(RoleDO bean);
}
