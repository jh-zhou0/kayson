package cn.zjh.kayson.module.system.dal.mysql.user;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.mybatis.core.mapper.BaseMapperX;
import cn.zjh.kayson.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.zjh.kayson.module.system.controller.admin.user.vo.user.UserPageReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/** 
 * AdminUserDO 对应的 Mapper 层
 * 
 * @author zjh - kayson
 */
@Mapper
public interface AdminUserMapper extends BaseMapperX<AdminUserDO> {
    
    default AdminUserDO selectByUsername(String username) {
        return selectOne(AdminUserDO::getUsername, username);
    }

    default AdminUserDO selectByMobile(String mobile) {
        return selectOne(AdminUserDO::getMobile, mobile);
    }

    default AdminUserDO selectByEmail(String email) {
        return selectOne(AdminUserDO::getEmail, email);
    }
    
    default PageResult<AdminUserDO> selectPage(UserPageReqVO reqVO, Collection<Long> deptIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AdminUserDO>()
                .likeIfPresent(AdminUserDO::getUsername, reqVO.getUsername())
                .likeIfPresent(AdminUserDO::getMobile, reqVO.getMobile())
                .eqIfPresent(AdminUserDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(AdminUserDO::getCreateTime, reqVO.getCreateTime())
                .inIfPresent(AdminUserDO::getDeptId, deptIds)
                .orderByDesc(AdminUserDO::getId));
    }

    default List<AdminUserDO> selectListByStatus(Integer status) {
        return selectList(AdminUserDO::getStatus, status);
    }
}
