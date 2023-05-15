package cn.zjh.kayson.module.system.service.permission;

import cn.zjh.kayson.module.system.dal.dataobject.permission.MenuDO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleDO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 权限 Service 接口
 * 提供用户-角色、角色-菜单、角色-部门的关联权限处理
 * 
 * @author zjh - kayson
 */
public interface PermissionService {

    /**
     * 设置用户角色
     *
     * @param userId 角色编号
     * @param roleIds 角色编号集合
     */
    void assignUserRole(Long userId, Set<Long> roleIds);

    /**
     * 获得用户拥有的角色编号集合
     *
     * @param userId 用户编号
     * @return 角色编号集合
     */
    Set<Long> getUserRoleIds(Long userId);

    /**
     * 设置角色菜单
     *
     * @param roleId 角色编号
     * @param menuIds 菜单编号集合
     */
    void assignRoleMenu(Long roleId, Set<Long> menuIds);

    /**
     * 获得角色拥有的菜单编号集合
     *
     * @param roleId 角色编号
     * @return 菜单编号集合
     */
    Set<Long> getRoleMenuIds(Long roleId);

    /**
     * 获得角色们拥有的菜单列表
     *
     * @param roleList 角色数组
     * @return 菜单列表
     */
    List<MenuDO> getRoleMenuList(Collection<RoleDO> roleList);
}
