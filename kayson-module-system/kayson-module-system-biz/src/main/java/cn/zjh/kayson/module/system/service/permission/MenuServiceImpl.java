package cn.zjh.kayson.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuUpdateReqVO;
import cn.zjh.kayson.module.system.convert.permission.MenuConvert;
import cn.zjh.kayson.module.system.dal.dataobject.permission.MenuDO;
import cn.zjh.kayson.module.system.dal.mysql.permission.MenuMapper;
import cn.zjh.kayson.module.system.enums.permission.MenuTypeEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.zjh.kayson.framework.common.exception.util.ServiceExceptionUtils.exception;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;

/**
 * 菜单 Service 实现
 * 
 * @author zjh - kayson
 */
@Service
public class MenuServiceImpl implements MenuService {
    
    @Resource
    private MenuMapper menuMapper;
    
    @Resource
    private PermissionService permissionService;
    
    @Override
    public Long createMenu(MenuCreateReqVO reqVO) {
        // 校验正确性
        validateForCreateOrUpdate(null, reqVO.getParentId(), reqVO.getName());
        // 插入菜单
        MenuDO menu = MenuConvert.INSTANCE.convert(reqVO);
        initMenuProperty(menu);
        menuMapper.insert(menu);
        return menu.getId();
    }

    @Override
    public void updateMenu(MenuUpdateReqVO reqVO) {
        // 校验正确性
        validateForCreateOrUpdate(reqVO.getId(), reqVO.getParentId(), reqVO.getName());
        // 更新菜单
        MenuDO updateObj = MenuConvert.INSTANCE.convert(reqVO);
        initMenuProperty(updateObj);
        menuMapper.updateById(updateObj);
    }

    @Override
    public void deleteMenu(Long id) {
        // 校验是否还有子菜单
        if (menuMapper.selectCountByParentId(id) > 0) {
            throw exception(MENU_EXISTS_CHILDREN);
        }
        // 校验删除的菜单是否存在
        validateMenuIdExist(id);
        // 删除菜单
        menuMapper.deleteById(id);
        // 删除授予给角色的权限
        permissionService.processMenuDeleted(id);
    }

    @Override
    public MenuDO getMenu(Long id) {
        return menuMapper.selectById(id);
    }

    @Override
    public List<MenuDO> getMenuList(MenuListReqVO reqVO) {
        return menuMapper.selectList(reqVO);
    }

    @Override
    public List<MenuDO> getMenuList() {
        return menuMapper.selectList();
    }

    @Override
    public List<MenuDO> getMenuList(Collection<Long> menuIds) {
        if (CollUtil.isEmpty(menuIds)) {
            return Collections.emptyList();
        }
        return menuMapper.selectBatchIds(menuIds);
    }

    private void validateForCreateOrUpdate(Long id, Long parentId, String name) {
        // 校验存在
        validateMenuIdExist(id);
        // 校验父菜单的有效性
        validateParentMenuEnable(id, parentId);
        // 校验菜单名的唯一性
        validateParentMenuNameUnique(id, parentId, name);
    }

    private void validateMenuIdExist(Long id) {
        if (id == null) {
            return;
        }
        MenuDO menu = menuMapper.selectById(id);
        if (menu == null) {
            throw exception(MENU_NOT_EXISTS);
        }
    }

    private void validateParentMenuEnable(Long id, Long parentId) {
        if (parentId == null || MenuDO.ID_ROOT.equals(parentId)) {
            return;
        }
        // 不能设置自己为父菜单
        if (parentId.equals(id)) {
            throw exception(MENU_PARENT_ERROR);
        }
        MenuDO menu = menuMapper.selectById(parentId);
        // 父菜单不存在
        if (menu == null) {
            throw exception(MENU_PARENT_NOT_EXISTS);
        }
        // 父菜单必须是目录或者菜单类型
        if (!MenuTypeEnum.DIR.getType().equals(menu.getType())
            && !MenuTypeEnum.MENU.getType().equals(menu.getType())) {
            throw exception(MENU_PARENT_NOT_DIR_OR_MENU);
        }
    }
    
    private void validateParentMenuNameUnique( Long id, Long parentId, String name) {
        MenuDO menu = menuMapper.selectByParentIdAndName(parentId, name);
        if (menu == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的菜单
        if (id == null) {
            throw exception(MENU_NAME_DUPLICATE);
        }
        if (!id.equals(menu.getId())) {
            throw exception(MENU_NAME_DUPLICATE);
        }
    }


    /**
     * 初始化菜单的通用属性。
     *
     * 例如说，只有目录或者菜单类型的菜单，才设置 icon
     *
     * @param menu 菜单
     */
    private void initMenuProperty(MenuDO menu) {
        // 菜单为按钮类型时，无需 component、icon、path 属性，进行置空
        if (MenuTypeEnum.BUTTON.getType().equals(menu.getType())) {
            menu.setComponent("");
            menu.setComponentName("");
            menu.setIcon("");
            menu.setPath("");
        }
    }
}
