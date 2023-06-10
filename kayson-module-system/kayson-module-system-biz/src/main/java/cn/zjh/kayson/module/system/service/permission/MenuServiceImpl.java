package cn.zjh.kayson.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.zjh.kayson.framework.common.util.collection.CollectionUtils;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.menu.MenuUpdateReqVO;
import cn.zjh.kayson.module.system.convert.permission.MenuConvert;
import cn.zjh.kayson.module.system.dal.dataobject.permission.MenuDO;
import cn.zjh.kayson.module.system.dal.mysql.permission.MenuMapper;
import cn.zjh.kayson.module.system.enums.permission.MenuTypeEnum;
import cn.zjh.kayson.module.system.mq.producer.permission.MenuProducer;
import cn.zjh.kayson.module.system.service.tenant.TenantService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.zjh.kayson.framework.common.exception.util.ServiceExceptionUtils.exception;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;

/**
 * 菜单 Service 实现
 * 
 * @author zjh - kayson
 */
@Service
@Slf4j
public class MenuServiceImpl implements MenuService {

    /**
     * 菜单缓存
     * key：菜单编号
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    @Getter
    @Setter
    private volatile Map<Long, MenuDO> menuCache;
    /**
     * 权限与菜单缓存
     * key：权限 {@link MenuDO#getPermission()}
     * value：MenuDO 数组，因为一个权限可能对应多个 MenuDO 对象
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    @Getter
    @Setter
    private volatile Multimap<String, MenuDO> permissionMenuCache;
    
    @Resource
    private MenuMapper menuMapper;
    
    @Resource
    private PermissionService permissionService;

    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private TenantService tenantService;
    
    @Resource
    private MenuProducer menuProducer;

    @Override
    @PostConstruct
    public void initLocalCache() {
        // 查询数据
        List<MenuDO> menuList = menuMapper.selectList();
        log.info("[initLocalCache][缓存菜单，数量为:{}]", menuList.size());

        // 构建缓存
        ImmutableMap.Builder<Long, MenuDO> menuCacheBuilder = ImmutableMap.builder();
        ImmutableMultimap.Builder<String, MenuDO> permissionMenuCacheBuilder = ImmutableMultimap.builder();
        menuList.forEach(menuDO -> {
            menuCacheBuilder.put(menuDO.getId(), menuDO);
            if (StrUtil.isNotEmpty(menuDO.getPermission())) { // 会存在 permission 为 null 的情况，导致 put 报 NPE 异常
                permissionMenuCacheBuilder.put(menuDO.getPermission(), menuDO);
            }
        });
        menuCache = menuCacheBuilder.build();
        permissionMenuCache = permissionMenuCacheBuilder.build();
    }

    @Override
    public Long createMenu(MenuCreateReqVO reqVO) {
        // 校验正确性
        validateForCreateOrUpdate(null, reqVO.getParentId(), reqVO.getName());
        // 插入菜单
        MenuDO menu = MenuConvert.INSTANCE.convert(reqVO);
        initMenuProperty(menu);
        menuMapper.insert(menu);
        // 发送刷新消息
        menuProducer.sendMenuRefreshMessage();
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
        // 发送刷新消息
        menuProducer.sendMenuRefreshMessage();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        // 发送刷新消息. 注意，需要事务提交后，在进行发送刷新消息。不然 db 还未提交，结果缓存先刷新了
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            
            @Override
            public void afterCommit() {
                menuProducer.sendMenuRefreshMessage();
            }
            
        });
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

    @Override
    public List<MenuDO> getMenuListByTenant(MenuListReqVO reqVO) {
        List<MenuDO> menuList = getMenuList(reqVO);
        // 开启多租户的情况下，需要过滤掉未开通的菜单
        tenantService.handleTenantMenu(menuIds -> menuList.removeIf(menu -> !CollUtil.contains(menuIds, menu.getId())));
        return menuList;
    }

    @Override
    public List<MenuDO> getMenuListFromCache(Collection<Integer> menuTypes, Collection<Integer> menusStatuses) {
        // 任一一个参数为空，则返回空
        if (CollectionUtils.isAnyEmpty(menuTypes, menusStatuses)) {
            return Collections.emptyList();
        }
        // 创建新数组，避免缓存被修改
        return menuCache.values().stream().filter(menuDO -> menuTypes.contains(menuDO.getType())
                && menusStatuses.contains(menuDO.getStatus())).collect(Collectors.toList());
    }

    @Override
    public List<MenuDO> getMenuListFromCache(Collection<Long> menuIds, Collection<Integer> menuTypes, Collection<Integer> menusStatuses) {
        // 任一一个参数为空，则返回空
        if (CollectionUtils.isAnyEmpty(menuIds, menuTypes, menusStatuses)) {
            return Collections.emptyList();
        }
        return menuCache.values().stream().filter(menuDO -> menuIds.contains(menuDO.getId())
                && menuTypes.contains(menuDO.getType())
                && menusStatuses.contains(menuDO.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MenuDO> getMenuListByPermissionFromCache(String permission) {
        if (StrUtil.isEmpty(permission)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(permissionMenuCache.get(permission));
    }

    private void validateForCreateOrUpdate(Long id, Long parentId, String name) {
        // 校验存在
        validateMenuIdExist(id);
        // 校验父菜单的有效性
        validateParentMenuEnable(id, parentId);
        // 校验菜单名的唯一性
        validateMenuNameUnique(id, parentId, name);
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

    @VisibleForTesting
    void validateParentMenuEnable(Long id, Long parentId) {
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
    
    @VisibleForTesting
    void validateMenuNameUnique(Long id, Long parentId, String name) {
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
