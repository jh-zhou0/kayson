package cn.zjh.kayson.module.system.dal.mysql.permission;

import cn.zjh.kayson.framework.mybatis.core.mapper.BaseMapperX;
import cn.zjh.kayson.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.zjh.kayson.module.system.dal.dataobject.permission.UserRoleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface UserRoleMapper extends BaseMapperX<UserRoleDO> {
    
    default List<UserRoleDO> selectListByUserId(Long userId) {
        return selectList(UserRoleDO::getUserId, userId);
    }

    default void deleteListByUserIdAndRoleIdIds(Long userId, Collection<Long> deleteRoleIds) {
        delete(new LambdaQueryWrapperX<UserRoleDO>()
                .eq(UserRoleDO::getUserId, userId)
                .in(UserRoleDO::getRoleId, deleteRoleIds));
    }
}
