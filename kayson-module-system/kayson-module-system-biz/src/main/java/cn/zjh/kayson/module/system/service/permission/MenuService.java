package cn.zjh.kayson.module.system.service.permission;

import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.MenuDO;

import java.util.List;

/**
 * 菜单 Service 接口
 * 
 * @author zjh - kayson
 */
public interface MenuService {

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
}
