package cn.zjh.kayson.module.system.convert.user;

import cn.zjh.kayson.module.system.controller.admin.user.vo.profile.UserProfileRespVO;
import cn.zjh.kayson.module.system.controller.admin.user.vo.profile.UserProfileUpdateReqVO;
import cn.zjh.kayson.module.system.controller.admin.user.vo.user.*;
import cn.zjh.kayson.module.system.dal.dataobject.dept.DeptDO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleDO;
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
    
    UserPageItemRespVO.Dept convert(DeptDO bean);

    List<UserSimpleRespVO> convertList(List<AdminUserDO> list);

    UserProfileRespVO convert02(AdminUserDO user);

    List<UserProfileRespVO.Role> convertList02(List<RoleDO> roleList);

    UserProfileRespVO.Dept convert02(DeptDO deptDO);

    AdminUserDO convert02(UserProfileUpdateReqVO reqVO);

    UserExcelVO convert01(AdminUserDO bean);

    AdminUserDO convert(UserImportExcelVO importUser);
    
}
