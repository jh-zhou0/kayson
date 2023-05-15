package cn.zjh.kayson.module.system.convert.auth;

import cn.zjh.kayson.framework.common.util.collection.CollectionUtils;
import cn.zjh.kayson.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import cn.zjh.kayson.module.system.controller.admin.auth.vo.AuthMenuRespVO;
import cn.zjh.kayson.module.system.controller.admin.auth.vo.AuthPermissionInfoRespVO;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.MenuDO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleDO;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface AuthConvert {

    AuthConvert INSTANCE = Mappers.getMapper(AuthConvert.class);

    AuthLoginRespVO convert(OAuth2AccessTokenDO bean);

    default AuthPermissionInfoRespVO convert(AdminUserDO user, List<RoleDO> roleList, List<MenuDO> menuList) {
        return new AuthPermissionInfoRespVO()
                .setUser(new AuthPermissionInfoRespVO.UserVO()
                        .setId(user.getId())
                        .setNickname(user.getNickname())
                        .setAvatar(user.getAvatar()))
                .setRoles(CollectionUtils.convertSet(roleList, RoleDO::getCode))
                .setPermissions(CollectionUtils.convertSet(menuList, MenuDO::getPermission));
    }

    AuthMenuRespVO convertTreeNode(MenuDO menu);
    
    /**
     * 将菜单列表，构建成菜单树
     *
     * @param menuList 菜单列表
     * @return 菜单树
     */
    default List<AuthMenuRespVO> buildMenuTree(List<MenuDO> menuList) {
        // 排序，保证菜单的有序性
        menuList.sort(Comparator.comparing(MenuDO::getSort));
        // 构建菜单树
        // 使用 LinkedHashMap 的原因，是为了排序 
        LinkedHashMap<Long, AuthMenuRespVO> treeNodeMap = new LinkedHashMap<>();
        menuList.forEach(menu -> treeNodeMap.put(menu.getId(), AuthConvert.INSTANCE.convertTreeNode(menu)));
        // 处理父子关系
        treeNodeMap.values().stream().filter(node -> !MenuDO.ID_ROOT.equals(node.getParentId())).forEach(childNode -> {
            // 获得父节点
            AuthMenuRespVO parentNode = treeNodeMap.get(childNode.getParentId());
            if (parentNode == null) {
                LoggerFactory.getLogger(getClass()).error("[buildRouterTree][resource({}) 找不到父资源({})]", 
                        childNode.getId(), childNode.getParentId());
                return;
            }
            // 将自己添加到父节点中
            if (parentNode.getChildren() == null) {
                parentNode.setChildren(new ArrayList<>());
            }
            parentNode.getChildren().add(childNode);
        });
        return CollectionUtils.filterList(treeNodeMap.values(), node -> MenuDO.ID_ROOT.equals(node.getParentId()));
    }
}
