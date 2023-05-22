package cn.zjh.kayson.module.system.service.permission;

import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.test.core.ut.BaseDbUnitTest;
import cn.zjh.kayson.module.system.dal.dataobject.permission.MenuDO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleDO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleMenuDO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.UserRoleDO;
import cn.zjh.kayson.module.system.dal.mysql.permission.RoleMenuMapper;
import cn.zjh.kayson.module.system.dal.mysql.permission.UserRoleMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static cn.zjh.kayson.framework.common.util.collection.SetUtils.asSet;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.*;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

/**
 * @author zjh - kayson
 */
@Import(PermissionServiceImpl.class)
public class PermissionServiceImplTest extends BaseDbUnitTest {
    
    @Resource
    private PermissionServiceImpl permissionService;
    
    @Resource
    private UserRoleMapper userRoleMapper;
    
    @Resource
    private RoleMenuMapper roleMenuMapper;
    
    @MockBean
    private RoleService roleService;
    
    @MockBean
    private MenuService menuService;

    @Test
    void testGetRoleMenuList_superAdmin() {
        // 准备参数
        List<RoleDO> roleList = Collections.singletonList(randomPojo(RoleDO.class, o -> o.setId(1L)));
        
        // mock 方法
        when(roleService.hasAnySuperAdmin(same(roleList))).thenReturn(true);
        List<MenuDO> menuList = randomPojoList(MenuDO.class);
        when(menuService.getMenuList()).thenReturn(menuList);
        
        // 调用
        List<MenuDO> menuDOList = permissionService.getRoleMenuList(roleList);
        // 断言
        assertEquals(menuList, menuDOList);
    }

    @Test
    void testGetRoleMenuList_normal() {
        // 准备参数
        List<RoleDO> roleList = Arrays.asList(
                randomPojo(RoleDO.class, o -> o.setId(1L)), randomPojo(RoleDO.class, o -> o.setId(2L)));
        Set<Long> menuIds = asSet(100L, 101L, 102L);

        // mock 数据
        List<RoleMenuDO> roleMenuDOList = Arrays.asList(
                randomPojo(RoleMenuDO.class, o -> o.setRoleId(1L).setMenuId(100L)),
                randomPojo(RoleMenuDO.class, o -> o.setRoleId(2L).setMenuId(101L)),
                randomPojo(RoleMenuDO.class, o -> o.setRoleId(2L).setMenuId(102L))
        );
        roleMenuMapper.insertBatch(roleMenuDOList);
        
        // mock 方法
        List<MenuDO> menuList = randomPojoList(MenuDO.class);
        when(menuService.getMenuList(menuIds)).thenReturn(menuList);
        
        // 调用
        List<MenuDO> menuDOList = permissionService.getRoleMenuList(roleList);
        // 断言
        assertEquals(menuList, menuDOList);
    }

    @Test
    void testGetUserRoleIds() {
        // 准备参数
        Long userId = 1L;
        Set<Long> roleIds = asSet(100L, 101L);
        
        // mock 数据
        List<UserRoleDO> userRoleDOList = Arrays.asList(
                randomPojo(UserRoleDO.class, o -> o.setUserId(1L).setRoleId(100L)),
                randomPojo(UserRoleDO.class, o -> o.setUserId(1L).setRoleId(101L))
        );
        userRoleMapper.insertBatch(userRoleDOList);
        
        // mock 方法
        List<RoleDO> roleDOList = Arrays.asList(
                randomPojo(RoleDO.class, o -> o.setId(100L).setStatus(CommonStatusEnum.ENABLE.getStatus())), 
                randomPojo(RoleDO.class, o -> o.setId(101L).setStatus(CommonStatusEnum.ENABLE.getStatus()))
        );
        when(roleService.getRoleList(roleIds)).thenReturn(roleDOList);
        
        // 调用
        Set<Long> userRoleIds = permissionService.getUserRoleIds(userId);
        // 断言
        assertEquals(roleIds, userRoleIds);
    }

    @Test
    void testGetRoleMenuIds_superAdmin() {
        // 准备参数
        Long roleId = 100L;
        // mock 方法
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setId(100L).setCode("super_admin"));
        when(roleService.getRole(eq(100L))).thenReturn(roleDO);
        List<MenuDO> menuList = singletonList(randomPojo(MenuDO.class).setId(1L));
        when(menuService.getMenuList()).thenReturn(menuList);

        // 调用
        Set<Long> menuIds = permissionService.getRoleMenuIds(roleId);
        // 断言
        assertEquals(singleton(1L), menuIds);
    }

    @Test
    void testGetRoleMenuIds_normal() {
        // 准备参数
        Long roleId = 100L;
        // mock 数据
        when(roleService.getRole(eq(100L))).thenReturn(randomPojo(RoleDO.class, o -> o.setId(100L)));
        RoleMenuDO roleMenu01 = randomPojo(RoleMenuDO.class).setRoleId(100L).setMenuId(1L);
        roleMenuMapper.insert(roleMenu01);
        RoleMenuDO roleMenu02 = randomPojo(RoleMenuDO.class).setRoleId(100L).setMenuId(2L);
        roleMenuMapper.insert(roleMenu02);

        // 调用
        Set<Long> menuIds = permissionService.getRoleMenuIds(roleId);
        // 断言
        assertEquals(asSet(1L, 2L), menuIds);
    }

    @Test
    void testAssignRoleMenu() {
        // 准备参数
        Long roleId = 1L;
        Set<Long> menuIds = asSet(200L, 300L);
        // mock 数据
        RoleMenuDO roleMenu01 = randomPojo(RoleMenuDO.class).setRoleId(1L).setMenuId(100L);
        roleMenuMapper.insert(roleMenu01);
        RoleMenuDO roleMenu02 = randomPojo(RoleMenuDO.class).setRoleId(1L).setMenuId(200L);
        roleMenuMapper.insert(roleMenu02);

        // 调用
        permissionService.assignRoleMenu(roleId, menuIds);
        // 断言
        List<RoleMenuDO> roleMenuList = roleMenuMapper.selectList();
        assertEquals(2, roleMenuList.size());
        assertEquals(1L, roleMenuList.get(0).getRoleId());
        assertEquals(200L, roleMenuList.get(0).getMenuId());
        assertEquals(1L, roleMenuList.get(1).getRoleId());
        assertEquals(300L, roleMenuList.get(1).getMenuId());
    }

    @Test
    void testAssignUserRole() {
        // 准备参数
        Long userId = 1L;
        Set<Long> roleIds = asSet(200L, 300L);
        // mock 数据
        UserRoleDO userRole01 = randomPojo(UserRoleDO.class).setUserId(1L).setRoleId(100L);
        userRoleMapper.insert(userRole01);
        UserRoleDO userRole02 = randomPojo(UserRoleDO.class).setUserId(1L).setRoleId(200L);
        userRoleMapper.insert(userRole02);

        // 调用
        permissionService.assignUserRole(userId, roleIds);
        // 断言
        List<UserRoleDO> userRoleDOList = userRoleMapper.selectList();
        assertEquals(2, userRoleDOList.size());
        assertEquals(1L, userRoleDOList.get(0).getUserId());
        assertEquals(200L, userRoleDOList.get(0).getRoleId());
        assertEquals(1L, userRoleDOList.get(1).getUserId());
        assertEquals(300L, userRoleDOList.get(1).getRoleId());
    }

    @Test
    void testProcessRoleDeleted() {
        // 准备参数
        Long roleId = randomLong();
        // mock 数据 UserRole
        UserRoleDO userRoleDO01 = randomPojo(UserRoleDO.class, o -> o.setRoleId(roleId)); // 被删除
        userRoleMapper.insert(userRoleDO01);
        UserRoleDO userRoleDO02 = randomPojo(UserRoleDO.class); // 不被删除
        userRoleMapper.insert(userRoleDO02);
        // mock 数据 RoleMenu
        RoleMenuDO roleMenuDO01 = randomPojo(RoleMenuDO.class, o -> o.setRoleId(roleId)); // 被删除
        roleMenuMapper.insert(roleMenuDO01);
        RoleMenuDO roleMenuDO02 = randomPojo(RoleMenuDO.class); // 不被删除
        roleMenuMapper.insert(roleMenuDO02);

        // 调用
        permissionService.processRoleDeleted(roleId);
        // 断言数据 RoleMenuDO
        List<RoleMenuDO> dbRoleMenus = roleMenuMapper.selectList();
        assertEquals(1, dbRoleMenus.size());
        assertPojoEquals(dbRoleMenus.get(0), roleMenuDO02);
        // 断言数据 UserRoleDO
        List<UserRoleDO> dbUserRoles = userRoleMapper.selectList();
        assertEquals(1, dbUserRoles.size());
        assertPojoEquals(dbUserRoles.get(0), userRoleDO02);
    }

    @Test
    void testProcessMenuDeleted() {
        // 准备参数
        Long menuId = randomLong();
        // mock 数据
        RoleMenuDO roleMenuDO01 = randomPojo(RoleMenuDO.class, o -> o.setMenuId(menuId)); // 被删除
        roleMenuMapper.insert(roleMenuDO01);
        RoleMenuDO roleMenuDO02 = randomPojo(RoleMenuDO.class); // 不被删除
        roleMenuMapper.insert(roleMenuDO02);

        // 调用
        permissionService.processMenuDeleted(menuId);
        // 断言数据
        List<RoleMenuDO> dbRoleMenus = roleMenuMapper.selectList();
        assertEquals(1, dbRoleMenus.size());
        assertPojoEquals(dbRoleMenus.get(0), roleMenuDO02);
    }

    @Test
    void testProcessUserDeleted() {
        // 准备参数
        Long userId = randomLong();
        // mock 数据
        UserRoleDO userRoleDO01 = randomPojo(UserRoleDO.class, o -> o.setUserId(userId)); // 被删除
        userRoleMapper.insert(userRoleDO01);
        UserRoleDO userRoleDO02 = randomPojo(UserRoleDO.class); // 不被删除
        userRoleMapper.insert(userRoleDO02);

        // 调用
        permissionService.processUserDeleted(userId);
        // 断言数据
        List<UserRoleDO> dbUserRoles = userRoleMapper.selectList();
        assertEquals(1, dbUserRoles.size());
        assertPojoEquals(dbUserRoles.get(0), userRoleDO02);
    }

    @Test
    void testHasAnyPermissions_superAdmin() {
       
    }

    @Test
    void testHasAnyPermissions_normal() {
        
    }

    @Test
    void testHasAnyRoles_superAdmin() {
        
    }

    @Test
    void testHasAnyRoles_normal() {
        
    }
}
