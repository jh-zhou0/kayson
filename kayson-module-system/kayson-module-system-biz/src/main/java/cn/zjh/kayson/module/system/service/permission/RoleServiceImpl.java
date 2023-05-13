package cn.zjh.kayson.module.system.service.permission;

import cn.hutool.core.util.ObjectUtil;
import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RoleCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RoleUpdateReqVO;
import cn.zjh.kayson.module.system.convert.permission.RoleConvert;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleDO;
import cn.zjh.kayson.module.system.dal.mysql.permission.RoleMapper;
import cn.zjh.kayson.module.system.enums.permission.DataScopeEnum;
import cn.zjh.kayson.module.system.enums.permission.RoleCodeEnum;
import cn.zjh.kayson.module.system.enums.permission.RoleTypeEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static cn.zjh.kayson.framework.common.exception.util.ServiceExceptionUtils.exception;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;

/**
 * 角色 Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
public class RoleServiceImpl implements RoleService {
    
    @Resource
    private RoleMapper roleMapper;
    
    @Override
    public Long createRole(RoleCreateReqVO reqVO, Integer type) {
        // 校验角色名和编码
        validateRoleDuplicate(null, reqVO.getName(), reqVO.getCode());
        // 插入角色
        RoleDO role = RoleConvert.INSTANCE.convert(reqVO);
        role.setType(ObjectUtil.defaultIfNull(type, RoleTypeEnum.CUSTOM.getType()));
        role.setStatus(CommonStatusEnum.ENABLE.getValue());
        role.setDataScope(DataScopeEnum.ALL.getScope()); // 默认可查看所有数据。原因是，可能一些项目不需要数据权限
        roleMapper.insert(role);
        return role.getId();
    }

    @Override
    public void updateRole(RoleUpdateReqVO reqVO) {
        // 校验角色存在
        validateRoleForUpdate(reqVO.getId());
        // 校验角色名和编码
        validateRoleDuplicate(reqVO.getId(), reqVO.getName(), reqVO.getCode());
        // 更新角色
        RoleDO updateObj = RoleConvert.INSTANCE.convert(reqVO);
        roleMapper.updateById(updateObj);
    }

    @Override
    public void deleteRole(Long id) {
        // 校验角色存在
        validateRoleForUpdate(id);
        // 删除角色
        roleMapper.selectById(id);
        // TODO: 删除角色相关数据
    }

    @Override
    public RoleDO getRole(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public PageResult<RoleDO> getRolePage(RolePageReqVO reqVO) {
        return roleMapper.selectPage(reqVO);
    }

    private void validateRoleForUpdate(Long id) {
        RoleDO role = roleMapper.selectById(id);
        if (role == null) {
            throw exception(ROLE_NOT_EXISTS);
        }
        // 内置角色，不允许更新
        if (RoleTypeEnum.SYSTEM.getType().equals(role.getType())) {
            throw exception(ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE);
        }
    }

    /**
     * 校验角色的唯一字段是否重复
     *
     * 1. 是否存在相同名字的角色
     * 2. 是否存在相同编码的角色
     *
     * @param id 角色编号
     * @param name 角色名字
     * @param code 角色编码
     */
    private void validateRoleDuplicate(Long id, String name, String code) {
        // 0. 超级管理员，不允许创建
        if (RoleCodeEnum.isSuperAdmin(code)) {
            throw exception(ROLE_ADMIN_CODE_ERROR, code);
        }
        // 1. 该 name 名字被其它角色所使用
        RoleDO role = roleMapper.selectByName(name);
        if (role != null && !role.getId().equals(id)) {
            throw exception(ROLE_NAME_DUPLICATE, name);
        }
        // 2. 是否存在相同编码的角色
        // 该 code 编码被其它角色所使用
        role = roleMapper.selectByCode(code);
        if (role != null && !role.getId().equals(id)) {
            throw exception(ROLE_CODE_DUPLICATE, code);
        }
    }
}
