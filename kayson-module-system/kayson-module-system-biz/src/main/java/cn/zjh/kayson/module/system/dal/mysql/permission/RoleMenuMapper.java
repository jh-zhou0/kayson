package cn.zjh.kayson.module.system.dal.mysql.permission;

import cn.zjh.kayson.framework.mybatis.core.mapper.BaseMapperX;
import cn.zjh.kayson.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleMenuDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface RoleMenuMapper extends BaseMapperX<RoleMenuDO> {

    default List<RoleMenuDO> selectListByRoleId(Long roleId) {
        return selectList(RoleMenuDO::getRoleId, roleId);
    }

    default void deleteListByRoleIdAndMenuIds(Long roleId, Collection<Long> deleteMenuIds) {
        delete(new LambdaQueryWrapperX<RoleMenuDO>()
                .eq(RoleMenuDO::getRoleId, roleId)
                .in(RoleMenuDO::getMenuId, deleteMenuIds));
    }

    default void deleteListByMenuId(Long menuId) {
        delete(new LambdaQueryWrapperX<RoleMenuDO>().eq(RoleMenuDO::getMenuId, menuId));
    }

    default void deleteListByRoleId(Long roleId) {
        delete(new LambdaQueryWrapperX<RoleMenuDO>().eq(RoleMenuDO::getRoleId, roleId));
    }
    
}
