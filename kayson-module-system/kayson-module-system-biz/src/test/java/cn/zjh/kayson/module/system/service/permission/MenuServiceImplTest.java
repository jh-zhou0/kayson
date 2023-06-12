package cn.zjh.kayson.module.system.service.permission;

import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.test.core.ut.BaseDbUnitTest;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.MenuDO;
import cn.zjh.kayson.module.system.dal.mysql.permission.MenuMapper;
import cn.zjh.kayson.module.system.enums.permission.MenuTypeEnum;
import cn.zjh.kayson.module.system.mq.producer.permission.MenuProducer;
import cn.zjh.kayson.module.system.service.tenant.TenantService;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.*;

import static cn.zjh.kayson.framework.common.util.collection.SetUtils.asSet;
import static cn.zjh.kayson.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.*;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

/**
 * @author zjh - kayson
 */
@Import(MenuServiceImpl.class)
public class MenuServiceImplTest extends BaseDbUnitTest {
    
    @Resource
    private MenuServiceImpl menuService;
    
    @Resource
    private MenuMapper menuMapper;
    
    @MockBean
    private PermissionService permissionService;

    @MockBean
    private TenantService tenantService;
    
    @MockBean
    private MenuProducer menuProducer;

    @Test
    void testInitLocalCache() {
        // mock 数据
        MenuDO menuDO1 = randomPojo(MenuDO.class);
        menuMapper.insert(menuDO1);
        MenuDO menuDO2 = randomPojo(MenuDO.class);
        menuMapper.insert(menuDO2);
        
        // 调用
        menuService.initLocalCache();
        // 校验 menuCache 缓存
        Map<Long, MenuDO> menuCache = menuService.getMenuCache();
        assertEquals(2, menuCache.size());
        assertPojoEquals(menuDO1, menuCache.get(menuDO1.getId()));
        assertPojoEquals(menuDO2, menuCache.get(menuDO2.getId()));
        // 校验 permissionCache 缓存
        Multimap<String, MenuDO> permissionMenuCache = menuService.getPermissionMenuCache();
        assertEquals(2, permissionMenuCache.size());
        assertPojoEquals(menuDO1, permissionMenuCache.get(menuDO1.getPermission()));
        assertPojoEquals(menuDO2, permissionMenuCache.get(menuDO2.getPermission()));
    }

    @Test
    void testCreateMenu_success() {
        // mock 数据（构造父菜单）
        MenuDO parent = randomPojo(MenuDO.class, o -> 
                o.setType(MenuTypeEnum.MENU.getType()).setName("parent").setParentId(0L)
        );
        menuMapper.insert(parent);
        // 准备参数
        MenuCreateReqVO reqVO = randomPojo(MenuCreateReqVO.class,
                o -> o.setParentId(parent.getId()).setName("children").setType(MenuTypeEnum.MENU.getType())
        );
        
        // 调用
        Long menuId = menuService.createMenu(reqVO);
        // 校验记录的属性是否正确
        MenuDO menuDO = menuMapper.selectById(menuId);
        assertPojoEquals(reqVO, menuDO);
        // 校验调用
        verify(menuProducer).sendMenuRefreshMessage();
    }
    
    @Test
    void testUpdateMenu_success() {
        // mock 数据（构造父子菜单）
        MenuDO parent = randomPojo(MenuDO.class, o ->
                o.setType(MenuTypeEnum.MENU.getType()).setName("parent").setParentId(0L)
        );
        menuMapper.insert(parent);
        MenuDO children = randomPojo(MenuDO.class, o ->
                o.setType(MenuTypeEnum.MENU.getType()).setName("children").setParentId(parent.getId())
        );
        menuMapper.insert(children);
        Long id = children.getId();

        // 准备参数
        MenuUpdateReqVO reqVO = randomPojo(MenuUpdateReqVO.class, o -> {
            o.setId(id);
            o.setType(MenuTypeEnum.MENU.getType());
            o.setName("test_updateName");
            o.setParentId(children.getParentId());
        });
        
        // 调用
        menuService.updateMenu(reqVO);
        // 校验记录的属性是否正确
        MenuDO menuDO = menuMapper.selectById(id);
        assertPojoEquals(reqVO, menuDO);
        // 校验调用
        verify(menuProducer).sendMenuRefreshMessage();
    }

    @Test
    void testUpdateMenu_sonIdNotExist() {
        // 准备参数
        MenuUpdateReqVO reqVO = randomPojo(MenuUpdateReqVO.class);
        // 调用，并断言异常
        assertServiceException(() -> menuService.updateMenu(reqVO), MENU_NOT_EXISTS);
    }

    @Test
    void testDeleteMenu_success() {
        // mock 数据
        MenuDO menuDO = randomPojo(MenuDO.class);
        menuMapper.insert(menuDO);
        // 准备参数
        Long id = menuDO.getId();
        
        // 调用
        menuService.deleteMenu(id);
        // 断言
        MenuDO dbMenuDO = menuMapper.selectById(id);
        assertNull(dbMenuDO);
        // 校验调用
        verify(permissionService).processMenuDeleted(id);
        verify(menuProducer).sendMenuRefreshMessage();
    }

    @Test
    void testDeleteMenu_menuNotExist() {
        // 调用，并断言异常
        assertServiceException(() -> menuService.deleteMenu(randomLong()), MENU_NOT_EXISTS);
    }

    @Test
    void testDeleteMenu_existChildren() {
        // mock 数据（构造父子菜单）
        MenuDO parent = randomPojo(MenuDO.class, o ->
                o.setType(MenuTypeEnum.MENU.getType()).setName("parent").setParentId(0L)
        );
        menuMapper.insert(parent);
        MenuDO children = randomPojo(MenuDO.class, o ->
                o.setType(MenuTypeEnum.MENU.getType()).setName("children").setParentId(parent.getId())
        );
        menuMapper.insert(children);
        Long id = parent.getId();

        // 调用并断言异常
        assertServiceException(() -> menuService.deleteMenu(id), MENU_EXISTS_CHILDREN);
    }

    @Test
    void testGetMenuList_all() {
        // mock 数据
        MenuDO menu100 = randomPojo(MenuDO.class);
        menuMapper.insert(menu100);
        MenuDO menu101 = randomPojo(MenuDO.class);
        menuMapper.insert(menu101);
        // 准备参数

        // 调用
        List<MenuDO> list = menuService.getMenuList();
        // 断言
        assertEquals(2, list.size());
        assertPojoEquals(menu100, list.get(0));
        assertPojoEquals(menu101, list.get(1));
    }

    @Test
    void testGetMenuList() {
        // mock 数据
        MenuDO menuDO = randomPojo(MenuDO.class, o ->
                o.setName("kayson").setStatus(CommonStatusEnum.ENABLE.getStatus())
        );
        menuMapper.insert(menuDO);
        // 测试 status 不匹配
        menuMapper.insert(cloneIgnoreId(menuDO, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 测试 name 不匹配
        menuMapper.insert(cloneIgnoreId(menuDO, o -> o.setName("hello")));
        
        // 准备参数
        MenuListReqVO reqVO = new MenuListReqVO().setName("k").setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        List<MenuDO> result = menuService.getMenuList(reqVO);
        // 断言
        assertEquals(1, result.size());
        assertPojoEquals(menuDO, result.get(0));
    }

    @Test
    void getMenuList_withIds() {
        // mock 数据
        MenuDO menuDO01 = randomPojo(MenuDO.class, o ->
                o.setName("kayson01").setStatus(randomCommonStatus())
        );
        menuMapper.insert(menuDO01);
        MenuDO menuDO02 = randomPojo(MenuDO.class, o ->
                o.setName("kayson02").setStatus(randomCommonStatus())
        );
        menuMapper.insert(menuDO02);
        
        // 准备参数
        List<Long> ids = Arrays.asList(menuDO01.getId(), menuDO02.getId());
        
        // 调用
        List<MenuDO> result = menuService.getMenuList(ids);
        // 断言
        assertEquals(2, result.size());
        assertPojoEquals(menuDO01, result.get(0));
        assertPojoEquals(menuDO02, result.get(1));
    }

    @Test
    public void testGetMenuListByTenant() {
        // mock 数据
        MenuDO menu100 = randomPojo(MenuDO.class, o -> o.setId(100L).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        menuMapper.insert(menu100);
        MenuDO menu101 = randomPojo(MenuDO.class, o -> o.setId(101L).setStatus(CommonStatusEnum.DISABLE.getStatus()));
        menuMapper.insert(menu101);
        MenuDO menu102 = randomPojo(MenuDO.class, o -> o.setId(102L).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        menuMapper.insert(menu102);
        // mock 过滤菜单
        Set<Long> menuIds = asSet(100L, 101L);
        doNothing().when(tenantService).handleTenantMenu(argThat(handler -> {
            handler.handle(menuIds);
            return true;
        }));
        // 准备参数
        MenuListReqVO reqVO = new MenuListReqVO().setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        List<MenuDO> result = menuService.getMenuListByTenant(reqVO);
        // 断言
        assertEquals(1, result.size());
        assertPojoEquals(menu100, result.get(0));
    }

    @Test
    void testGetMenu() {
        // mock 数据
        MenuDO menu = randomPojo(MenuDO.class);
        menuMapper.insert(menu);
        // 准备参数
        Long id = menu.getId();

        // 调用
        MenuDO dbMenu = menuService.getMenu(id);
        // 断言
        assertPojoEquals(menu, dbMenu);
    }

    @Test
    void testListMenusFromCache_withoutId() {
        // mock 缓存
        Map<Long, MenuDO> menuCache = new HashMap<>();
        // 可被匹配
        MenuDO menuDO = randomPojo(MenuDO.class, o -> o.setId(1L)
                .setType(MenuTypeEnum.MENU.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        menuCache.put(menuDO.getId(), menuDO);
        // 测试 type 不匹配
        menuCache.put(3L, randomPojo(MenuDO.class, o -> o.setId(3L)
                .setType(MenuTypeEnum.BUTTON.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus())));
        // 测试 status 不匹配
        menuCache.put(4L, randomPojo(MenuDO.class, o -> o.setId(4L)
                .setType(MenuTypeEnum.MENU.getType()).setStatus(CommonStatusEnum.DISABLE.getStatus())));
        menuService.setMenuCache(menuCache);

        // 准备参数
        Set<Integer> menuTypes = Collections.singleton(MenuTypeEnum.MENU.getType());
        Set<Integer> menuStatuses = Collections.singleton(CommonStatusEnum.ENABLE.getStatus());
        
        // 调用
        List<MenuDO> menuListFromCache = menuService.getMenuListFromCache(menuTypes, menuStatuses);
        // 断言
        assertEquals(1, menuListFromCache.size());
        assertPojoEquals(menuDO, menuListFromCache.get(0));
    }

    @Test
    void testListMenusFromCache_withId() {
        // mock 缓存
        Map<Long, MenuDO> menuCache = new HashMap<>();
        // 可被匹配
        MenuDO menuDO = randomPojo(MenuDO.class, o -> o.setId(1L)
                .setType(MenuTypeEnum.MENU.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        menuCache.put(menuDO.getId(), menuDO);
        // 测试 id 不匹配
        menuCache.put(2L, randomPojo(MenuDO.class, o -> o.setId(2L)
                .setType(MenuTypeEnum.MENU.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus())));
        // 测试 type 不匹配
        menuCache.put(3L, randomPojo(MenuDO.class, o -> o.setId(3L)
                .setType(MenuTypeEnum.BUTTON.getType()).setStatus(CommonStatusEnum.ENABLE.getStatus())));
        // 测试 status 不匹配
        menuCache.put(4L, randomPojo(MenuDO.class, o -> o.setId(4L)
                .setType(MenuTypeEnum.MENU.getType()).setStatus(CommonStatusEnum.DISABLE.getStatus())));
        menuService.setMenuCache(menuCache);
        
        // 准备参数
        Set<Long> menuIds = asSet(1L, 3L, 4L);
        Set<Integer> menuTypes = Collections.singleton(MenuTypeEnum.MENU.getType());
        Set<Integer> menuStatuses = Collections.singleton(CommonStatusEnum.ENABLE.getStatus());
        
        // 调用
        List<MenuDO> menuListFromCache = menuService.getMenuListFromCache(menuIds, menuTypes, menuStatuses);
        // 断言
        assertEquals(1, menuListFromCache.size());
        assertPojoEquals(menuDO, menuListFromCache.get(0));
    }

    @Test
    void testGetMenuListByPermissionFromCache() {
        // mock 缓存
        Multimap<String, MenuDO> permissionMenuCache = LinkedListMultimap.create();
        // 可被匹配
        MenuDO menuDO01 = randomPojo(MenuDO.class, o -> o.setId(1L).setPermission("123"));
        permissionMenuCache.put(menuDO01.getPermission(), menuDO01);
        MenuDO menuDO02 = randomPojo(MenuDO.class, o -> o.setId(2L).setPermission("123"));
        permissionMenuCache.put(menuDO02.getPermission(), menuDO02);
        // 不可匹配
        permissionMenuCache.put("456", randomPojo(MenuDO.class, o -> o.setId(3L).setPermission("456")));
        menuService.setPermissionMenuCache(permissionMenuCache);
        // 准备参数
        String permission = "123";
        
        // 调用
        List<MenuDO> menuListByPermissionFromCache = menuService.getMenuListByPermissionFromCache(permission);
        // 断言
        assertEquals(2, menuListByPermissionFromCache.size());
        assertPojoEquals(menuDO01, menuListByPermissionFromCache.get(0));
        assertPojoEquals(menuDO02, menuListByPermissionFromCache.get(1));
    }

    @Test
    void testValidateParentMenu_success() {
        // mock 数据
        MenuDO parent = randomPojo(MenuDO.class, o ->
                o.setType(MenuTypeEnum.MENU.getType()).setName("parent").setParentId(0L)
        );
        menuMapper.insert(parent);
        
        // 准备参数
        Long parentId = parent.getId();

        // 调用，无需断言
        menuService.validateParentMenuEnable(null, parentId);
    }

    @Test
    void testValidateParentMenu_canNotSetSelfToBeParent() {
        // 调用，并断言异常
        assertServiceException(() -> menuService.validateParentMenuEnable(1L, 1L), MENU_PARENT_ERROR);
    }

    @Test
    void testValidateParentMenu_parentNotExist() {
        // 调用，并断言异常
        assertServiceException(() -> menuService.validateParentMenuEnable(null, randomLong()), MENU_PARENT_NOT_EXISTS);
    }

    @Test
    void testValidateParentMenu_parentTypeError() {
        // mock 数据
        MenuDO parent = randomPojo(MenuDO.class, o ->
                o.setType(MenuTypeEnum.BUTTON.getType()).setName("parent").setParentId(0L)
        );
        menuMapper.insert(parent);

        // 调用，并断言异常
        assertServiceException(() -> menuService.validateParentMenuEnable(null, parent.getId()), MENU_PARENT_NOT_DIR_OR_MENU);
    }

    @Test
    void testValidateMenu_success() {
        // mock 数据（构造父子菜单）
        MenuDO parent = randomPojo(MenuDO.class, o ->
                o.setType(MenuTypeEnum.MENU.getType()).setName("parent").setParentId(0L)
        );
        menuMapper.insert(parent);
        MenuDO children = randomPojo(MenuDO.class, o ->
                o.setType(MenuTypeEnum.MENU.getType()).setName("children").setParentId(parent.getId())
        );
        menuMapper.insert(children);
        
        // 准备参数
        Long id = randomLong();
        Long parentId = parent.getId();
        String name = randomString();
        
        // 调用
        menuService.validateMenuNameUnique(id, parentId, name);
    }

    @Test
    void testValidateMenu_sonMenuNameDuplicate() {
        // mock 数据（构造父子菜单）
        MenuDO parent = randomPojo(MenuDO.class, o ->
                o.setType(MenuTypeEnum.MENU.getType()).setName("parent").setParentId(0L)
        );
        menuMapper.insert(parent);
        MenuDO children = randomPojo(MenuDO.class, o ->
                o.setType(MenuTypeEnum.MENU.getType()).setName("children").setParentId(parent.getId())
        );
        menuMapper.insert(children);

        // 准备参数
        Long id = randomLong();
        Long parentId = parent.getId();
        String name = children.getName(); // 相同名称

        // 调用，并断言异常
        assertServiceException(() -> menuService.validateMenuNameUnique(id, parentId, name), MENU_NAME_DUPLICATE);
    }
    
}
