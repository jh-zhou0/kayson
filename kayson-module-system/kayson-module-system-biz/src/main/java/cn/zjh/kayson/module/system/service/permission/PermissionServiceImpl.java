package cn.zjh.kayson.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.util.collection.CollectionUtils;
import cn.zjh.kayson.module.system.dal.dataobject.permission.MenuDO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleDO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleMenuDO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.UserRoleDO;
import cn.zjh.kayson.module.system.dal.mysql.permission.RoleMenuMapper;
import cn.zjh.kayson.module.system.dal.mysql.permission.UserRoleMapper;
import cn.zjh.kayson.module.system.enums.permission.RoleCodeEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 权限 Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
public class PermissionServiceImpl implements PermissionService {
    
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private RoleMenuMapper roleMenuMapper;
    
    @Resource
    private RoleService roleService;
    @Resource
    private MenuService menuService;
    
    @Override
    public void assignUserRole(Long userId, Set<Long> roleIds) {
        // 获得用户拥有角色编号
        Set<Long> dbRoleIds = getUserRoleIds(userId);
        // 计算新增和删除的角色编号
        Collection<Long> createRoleIds = CollUtil.subtract(roleIds, dbRoleIds);
        Collection<Long> deleteRoleIds = CollUtil.subtract(dbRoleIds, roleIds);
        // 执行新增和删除。对于已经授权的角色，不用做任何处理
        if (CollUtil.isNotEmpty(createRoleIds)) {
            List<UserRoleDO> userRoleDOList = CollectionUtils.convertList(createRoleIds, roleId ->
                    new UserRoleDO().setUserId(userId).setRoleId(roleId));
            userRoleMapper.insertBatch(userRoleDOList);
        }
        if (CollUtil.isNotEmpty(deleteRoleIds)) {
            userRoleMapper.deleteListByUserIdAndRoleIdIds(userId, deleteRoleIds);
        }
    }

    @Override
    public Set<Long> getUserRoleIds(Long userId) {
        List<UserRoleDO> userRoleDOList = userRoleMapper.selectListByUserId(userId);
        // 创建用户的时候没有分配角色，会存在空指针异常
        if (CollUtil.isEmpty(userRoleDOList)) {
            return Collections.emptySet();
        }
        // 过滤角色状态，只要开启的
        Set<Long> roleIds = CollectionUtils.convertSet(userRoleDOList, UserRoleDO::getRoleId);
        List<RoleDO> roleDOList = roleService.getRoleList(roleIds);
        return CollectionUtils.convertSet(roleDOList, RoleDO::getId, roleDO -> 
                CommonStatusEnum.ENABLE.getValue().equals(roleDO.getStatus()));
    }

    @Override
    public void assignRoleMenu(Long roleId, Set<Long> menuIds) {
        // 获得角色拥有菜单编号
        Set<Long> dbMenuIds = CollectionUtils.convertSet(
                roleMenuMapper.selectListByRoleId(roleId), RoleMenuDO::getMenuId);
        // 计算新增和删除的菜单编号
        Collection<Long> createMenuIds = CollUtil.subtract(menuIds, dbMenuIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbMenuIds, menuIds);
        // 执行新增和删除。对于已经授权的菜单，不用做任何处理
        if (CollUtil.isNotEmpty(createMenuIds)) {
            roleMenuMapper.insertBatch(CollectionUtils.convertList(createMenuIds, menuId -> 
                    new RoleMenuDO().setRoleId(roleId).setMenuId(menuId)));
        }
        if (CollUtil.isNotEmpty(deleteMenuIds)) {
            roleMenuMapper.deleteListByRoleIdAndMenuIds(roleId, deleteMenuIds);
        }
    }

    @Override
    public Set<Long> getRoleMenuIds(Long roleId) {
        RoleDO role = roleService.getRole(roleId);
        if (role == null) {
            return Collections.emptySet();
        }
        // 如果是超级管理员的情况下，获取全部菜单编号
        if (RoleCodeEnum.isSuperAdmin(role.getCode())) {
            List<MenuDO> menuList = menuService.getMenuList();
            return CollectionUtils.convertSet(menuList, MenuDO::getId);
        }
        // 如果是非管理员的情况下，获得拥有的菜单编号
        List<RoleMenuDO> roleMenuDOList = roleMenuMapper.selectListByRoleId(roleId);
        return CollectionUtils.convertSet(roleMenuDOList, RoleMenuDO::getMenuId);
    }

    @Override
    public List<MenuDO> getRoleMenuList(Collection<RoleDO> roleList) {
        if (CollUtil.isEmpty(roleList)) {
            return Collections.emptyList();
        }
        // 判断角色是否包含超级管理员。如果是超级管理员，获取到全部
        if (roleService.hasAnySuperAdmin(roleList)) {
            return menuService.getMenuList();
        }
        // 获得角色拥有的菜单关联
        Set<Long> roleIds = CollectionUtils.convertSet(roleList, RoleDO::getId);
        Set<Long> menuIds = new HashSet<>();
        roleIds.forEach(roleId -> {
            List<RoleMenuDO> roleMenuDOList = roleMenuMapper.selectListByRoleId(roleId);
            Set<Long> menuIdSet = CollectionUtils.convertSet(roleMenuDOList, RoleMenuDO::getMenuId);
            menuIds.addAll(menuIdSet);
        });
        return menuService.getMenuList(menuIds);
    }
}
