package cn.zjh.kayson.module.system.service.permission;

import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.test.core.ut.BaseDbUnitTest;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.MenuDO;
import cn.zjh.kayson.module.system.dal.mysql.permission.MenuMapper;
import cn.zjh.kayson.module.system.enums.permission.MenuTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static cn.zjh.kayson.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.*;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        verify(permissionService).processMenuDeleted(id);
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
