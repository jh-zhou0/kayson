package cn.zjh.kayson.module.system.service.permission;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RoleCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RoleExportReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RoleUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleDO;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 角色 Service 接口
 * 
 * @author zjh - kayson
 */
public interface RoleService {

    /**
     * 初始化角色的本地缓存
     */
    void initLocalCache();

    /**
     * 创建角色
     *
     * @param reqVO 创建角色信息
     * @param type 角色类型
     * @return 角色编号
     */
    Long createRole(@Valid RoleCreateReqVO reqVO, Integer type);

    /**
     * 更新角色
     *
     * @param reqVO 更新角色信息
     */
    void updateRole(@Valid RoleUpdateReqVO reqVO);

    /**
     * 更新角色状态
     *
     * @param id 角色编号
     * @param status 状态
     */
    void updateRoleStatus(Long id, Integer status);

    /**
     * 删除角色
     * 
     * @param id 角色编号
     */
    void deleteRole(Long id);

    /**
     * 获得角色
     *
     * @param id 角色编号
     * @return 角色
     */
    RoleDO getRole(Long id);

    /**
     * 获得角色，从缓存中
     *
     * @param id 角色编号
     * @return 角色
     */
    RoleDO getRoleFromCache(Long id);

    /**
     * 获得角色分页
     *
     * @param reqVO 角色分页查询
     * @return 角色分页结果
     */
    PageResult<RoleDO> getRolePage(RolePageReqVO reqVO);

    /**
     * 获得角色数组
     * 
     * @param ids 角色编号数组
     * @return 角色数组
     */
    List<RoleDO> getRoleList(Collection<Long> ids);

    /**
     * 获得角色数组，从缓存中
     *
     * @param ids 角色编号数组
     * @return 角色数组
     */
    List<RoleDO> getRoleListFromCache(Collection<Long> ids);

    /**
     * 获得角色列表
     *
     * @param statuses 筛选的状态。允许空，空时不筛选
     * @return 角色列表
     */
    List<RoleDO> getRoleListByStatus(@Nullable Collection<Integer> statuses);

    /**
     * 判断角色数组中，是否有超级管理员
     *
     * @param roleList 角色数组
     * @return 是否有管理员
     */
    boolean hasAnySuperAdmin(Collection<RoleDO> roleList);

    /**
     * 判断角色编号数组中，是否有管理员
     *
     * @param ids 角色编号数组
     * @return 是否有管理员
     */
    default boolean hasAnySuperAdmin(Set<Long> ids) {
        return hasAnySuperAdmin(getRoleListFromCache(ids));
    }

    /**
     * 获得角色列表
     *
     * @param reqVO 列表查询
     * @return 角色列表
     */
    List<RoleDO> getRoleList(RoleExportReqVO reqVO);
    
}
