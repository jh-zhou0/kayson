package cn.zjh.kayson.module.system.convert.user;

import cn.zjh.kayson.module.system.controller.admin.user.vo.user.UserCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.user.vo.user.UserPageItemRespVO;
import cn.zjh.kayson.module.system.controller.admin.user.vo.user.UserSimpleRespVO;
import cn.zjh.kayson.module.system.controller.admin.user.vo.user.UserUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface UserConvert {
    
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);
    
    AdminUserDO convert(UserCreateReqVO bean);
    
    AdminUserDO convert(UserUpdateReqVO bean);
    
    UserPageItemRespVO convert(AdminUserDO bean);

    List<UserSimpleRespVO> convertList04(List<AdminUserDO> list);
}
