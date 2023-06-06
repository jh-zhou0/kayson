package cn.zjh.kayson.module.system.api.permission;

import cn.zjh.kayson.module.system.api.permission.vo.DeptDataPermissionRespDTO;
import cn.zjh.kayson.module.system.service.permission.PermissionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zjh - kayson
 */
@Service
public class PermissionApiImpl implements PermissionApi {
    
    @Resource
    private PermissionService permissionService;
    
    @Override
    public boolean hasAnyPermissions(Long userId, String... permissions) {
        return permissionService.hasAnyPermissions(userId, permissions);
    }

    @Override
    public boolean hasAnyRoles(Long userId, String... roles) {
        return permissionService.hasAnyRoles(userId, roles);
    }

    @Override
    public DeptDataPermissionRespDTO getDeptDataPermission(Long userId) {
        return permissionService.getDeptDataPermission(userId);
    }
    
}
