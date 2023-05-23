package cn.zjh.kayson.module.system.service.permission;

import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.MenuDO;

import java.util.Collection;
import java.util.List;

/**
 * 菜单 Service 接口
 * 
 * @author zjh - kayson
 */
public interface MenuService {

    /**
     * 初始化菜单的本地缓存
     */
    void initLocalCache();

    /**
     * 创建菜单
     *
     * @param reqVO 菜单信息
     * @return 菜单编号
     */
    Long createMenu(MenuCreateReqVO reqVO);

    /**
     * 更新菜单
     * 
     * @param reqVO 更新信息
     */
    void updateMenu(MenuUpdateReqVO reqVO);

    /**
     * 删除菜单
     * 
     * @param id 菜单编号
     */
    void deleteMenu(Long id);

    /**
     * 获取菜单
     * 
     * @param id 菜单编号
     * @return 菜单信息
     */
    MenuDO getMenu(Long id);

    /**
     * 筛选菜单列表
     *
     * @param reqVO 筛选条件请求 VO
     * @return 菜单列表
     */
    List<MenuDO> getMenuList(MenuListReqVO reqVO);

    /**
     * 获得所有菜单列表
     *
     * @return 菜单列表
     */
    List<MenuDO> getMenuList();

    /**
     * 获得指定菜单列表
     * 
     * @param menuIds 菜单编号数组
     * @return 菜单列表
     */
    List<MenuDO> getMenuList(Collection<Long> menuIds);

    /**
     * 获得所有菜单，从缓存中
     *
     * 任一参数为空时，则返回为空
     *
     * @param menuTypes 菜单类型数组
     * @param menusStatuses 菜单状态数组
     * @return 菜单列表
     */
    List<MenuDO> getMenuListFromCache(Collection<Integer> menuTypes, Collection<Integer> menusStatuses);

    /**
     * 获得指定编号的菜单数组，从缓存中
     *
     * 任一参数为空时，则返回为空
     *
     * @param menuIds 菜单编号数组
     * @param menuTypes 菜单类型数组
     * @param menusStatuses 菜单状态数组
     * @return 菜单数组
     */
    List<MenuDO> getMenuListFromCache(Collection<Long> menuIds, Collection<Integer> menuTypes,
                                      Collection<Integer> menusStatuses);

    /**
     * 获得权限对应的菜单数组
     *
     * @param permission 权限标识
     * @return 数组
     */
    List<MenuDO> getMenuListByPermissionFromCache(String permission);
}
