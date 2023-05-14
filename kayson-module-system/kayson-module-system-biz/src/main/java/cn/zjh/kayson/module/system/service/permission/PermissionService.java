package cn.zjh.kayson.module.system.service.permission;

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
    
}
