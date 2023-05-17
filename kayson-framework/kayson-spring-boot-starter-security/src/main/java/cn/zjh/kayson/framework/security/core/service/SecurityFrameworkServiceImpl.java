package cn.zjh.kayson.framework.security.core.service;

import cn.zjh.kayson.module.system.api.permission.PermissionApi;
import lombok.RequiredArgsConstructor;

import static cn.zjh.kayson.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 默认的 {@link SecurityFrameworkService} 实现类
 * 
 * @author zjh - kayson
 */
@RequiredArgsConstructor
public class SecurityFrameworkServiceImpl implements SecurityFrameworkService{
    
    private final PermissionApi permissionApi;
    
    @Override
    public boolean hasPermission(String permission) {
        return hasAnyPermissions(permission);
    }

    @Override
    public boolean hasAnyPermissions(String... permissions) {
        return permissionApi.hasAnyPermissions(getLoginUserId(), permissions);
    }

    @Override
    public boolean hasRole(String role) {
        return hasAnyRoles(role);
    }

    @Override
    public boolean hasAnyRoles(String... roles) {
        return permissionApi.hasAnyRoles(getLoginUserId(), roles);
    }

    @Override
    public boolean hasScope(String scope) {
        return true;
    }

    @Override
    public boolean hasAnyScopes(String... scope) {
        return true;
    }
}
