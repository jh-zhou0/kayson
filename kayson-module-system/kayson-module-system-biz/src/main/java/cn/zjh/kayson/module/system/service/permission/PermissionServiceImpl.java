package cn.zjh.kayson.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.util.collection.CollectionUtils;
import cn.zjh.kayson.framework.common.util.collection.MapUtils;
import cn.zjh.kayson.module.system.dal.dataobject.permission.MenuDO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleDO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleMenuDO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.UserRoleDO;
import cn.zjh.kayson.module.system.dal.mysql.permission.RoleMenuMapper;
import cn.zjh.kayson.module.system.dal.mysql.permission.UserRoleMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

import static java.util.Collections.singleton;

/**
 * 权限 Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {
    
    /**
     * 用户编号与角色编号的缓存映射
     * key：用户编号
     * value：角色编号的数组
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    @Getter
    @Setter // 单元测试需要
    private volatile Multimap<Long, Long> userRoleCache;

    /**
     * 角色编号与菜单编号的缓存映射
     * key：角色编号
     * value：菜单编号的数组
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    @Getter
    @Setter // 单元测试需要
    private volatile Multimap<Long, Long> roleMenuCache;
    /**
     * 菜单编号与角色编号的缓存映射
     * key：菜单编号
     * value：角色编号的数组
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    @Getter
    @Setter // 单元测试需要
    private volatile Multimap<Long, Long> menuRoleCache;
    
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private RoleMenuMapper roleMenuMapper;
    
    @Resource
    private RoleService roleService;
    @Resource
    private MenuService menuService;

    @Override
    @PostConstruct
    public void initLocalCache() {
        initLocalCacheForUserRole();
        initLocalCacheForRoleMenu();
    }

    @VisibleForTesting
    void initLocalCacheForUserRole() {
        // 加载数据
        List<UserRoleDO> userRoleList = userRoleMapper.selectList();
        log.info("[initLocalCacheForUserRole][缓存用户与角色，数量为:{}]", userRoleList.size());
        
        // 构建缓存
        ImmutableMultimap.Builder<Long, Long> userRoleCacheBuilder = ImmutableMultimap.builder();
        userRoleList.forEach(userRoleDO -> userRoleCacheBuilder.put(userRoleDO.getUserId(), userRoleDO.getRoleId()));
        userRoleCache = userRoleCacheBuilder.build();
    }

    @VisibleForTesting
    void initLocalCacheForRoleMenu() {
        // 加载数据
        List<RoleMenuDO> roleMenuList = roleMenuMapper.selectList();
        log.info("[initLocalCacheForRoleMenu][缓存角色与菜单，数量为:{}]", roleMenuList.size());
        
        // 构建缓存
        ImmutableMultimap.Builder<Long, Long> roleMenuCacheBuilder = ImmutableMultimap.builder();
        ImmutableMultimap.Builder<Long, Long> menuRoleCacheBuilder = ImmutableMultimap.builder();
        roleMenuList.forEach(roleMenuDO -> {
            roleMenuCacheBuilder.put(roleMenuDO.getRoleId(), roleMenuDO.getMenuId());
            menuRoleCacheBuilder.put(roleMenuDO.getMenuId(), roleMenuDO.getRoleId());
        });
        roleMenuCache = roleMenuCacheBuilder.build();
        menuRoleCache = menuRoleCacheBuilder.build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRole(Long userId, Set<Long> roleIds) {
        // 获得用户拥有角色编号
        Set<Long> dbRoleIds = CollectionUtils.convertSet(
                userRoleMapper.selectListByUserId(userId), UserRoleDO::getRoleId);
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
        return CollectionUtils.convertSet(userRoleMapper.selectListByUserId(userId), UserRoleDO::getRoleId);
    }

    @Override
    public Set<Long> getUserRoleIdsFromCache(Long userId, Collection<Integer> roleStatuses) {
        Collection<Long> cacheRoleIds = userRoleCache.get(userId);
        // 创建用户的时候没有分配角色，会存在空指针异常
        if (CollUtil.isEmpty(cacheRoleIds)) {
            return Collections.emptySet();
        }
        Set<Long> roleIds = new HashSet<>(cacheRoleIds);
        // 过滤用户状态
        if (CollUtil.isNotEmpty(roleStatuses)) {
            roleIds.removeIf(roleId -> {
                RoleDO role = roleService.getRoleFromCache(roleId);
                return role == null || !roleStatuses.contains(role.getStatus());
            });
        }
        return roleIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        // 如果是超级管理员的情况下，获取全部菜单编号
        if (roleService.hasAnySuperAdmin(singleton(roleId))) {
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

    @Override
    public List<MenuDO> getRoleMenuListFromCache(Collection<Long> roleIds, Collection<Integer> menuTypes, Collection<Integer> menusStatuses) {
        // 任一一个参数为空时，不返回任何菜单
        if (CollectionUtils.isAnyEmpty(roleIds, menuTypes, menusStatuses)) {
            return Collections.emptyList();
        }
        // 判断角色是否包含超级管理员。如果是超级管理员，获取到全部
        List<RoleDO> roleList = roleService.getRoleListFromCache(roleIds);
        if (roleService.hasAnySuperAdmin(roleList)) {
            return menuService.getMenuListFromCache(menuTypes, menusStatuses);
        }
        // 获得角色拥有的菜单关联
        List<Long> menuIds = MapUtils.getList(roleMenuCache, roleIds);
        return menuService.getMenuListFromCache(menuIds, menuTypes, menusStatuses);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processUserDeleted(Long userId) {
        userRoleMapper.deleteListByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processMenuDeleted(Long menuId) {
        roleMenuMapper.deleteListByMenuId(menuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRoleDeleted(Long roleId) {
        userRoleMapper.deleteListByRoleId(roleId);
        roleMenuMapper.deleteListByRoleId(roleId);
    }

    @Override
    public boolean hasAnyPermissions(Long userId, String... permissions) {
        // 如果为空，说明已经有权限
        if (ArrayUtil.isEmpty(permissions)) {
            return true;
        }
        // 获得当前登录用户的角色。如果为空，说明没有权限
        Set<Long> roleIds = getUserRoleIdsFromCache(userId, singleton(CommonStatusEnum.ENABLE.getStatus()));
        if (CollUtil.isEmpty(roleIds)) {
            return false;
        }
        // 判断是否是超管。如果是，当然符合条件
        if (roleService.hasAnySuperAdmin(roleIds)) {
            return true;
        }
        // 遍历权限，判断是否有一个满足
        return Arrays.stream(permissions).anyMatch(permission -> {
            List<MenuDO> menuList = menuService.getMenuListByPermissionFromCache(permission);
            if (CollUtil.isEmpty(menuList)) {
                return false;
            }
            return menuList.stream().anyMatch(menu -> CollUtil.containsAny(roleIds, menuRoleCache.get(menu.getId())));
        });
    }

    @Override
    public boolean hasAnyRoles(Long userId, String... roles) {
        // 如果为空，说明已经有权限
        if (ArrayUtil.isEmpty(roles)) {
            return true;
        }
        // 获得当前登录用户的角色。如果为空，说明没有权限
        Set<Long> roleIds = getUserRoleIdsFromCache(userId, singleton(CommonStatusEnum.ENABLE.getStatus()));
        if (CollUtil.isEmpty(roleIds)) {
            return false;
        }
        // 判断是否是超管。如果是，当然符合条件
        if (roleService.hasAnySuperAdmin(roleIds)) {
            return true;
        }
        List<RoleDO> roleList = roleService.getRoleListFromCache(roleIds);
        Set<String> userRoles = CollectionUtils.convertSet(roleList, RoleDO::getCode);
        return CollUtil.containsAny(userRoles, Sets.newHashSet(roles));
    }
}
