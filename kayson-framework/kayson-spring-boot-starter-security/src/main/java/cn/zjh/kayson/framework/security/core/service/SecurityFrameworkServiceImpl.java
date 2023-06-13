package cn.zjh.kayson.framework.security.core.service;

import cn.hutool.core.collection.CollUtil;
import cn.zjh.kayson.framework.security.LoginUser;
import cn.zjh.kayson.framework.security.core.util.SecurityFrameworkUtils;
import cn.zjh.kayson.module.system.api.permission.PermissionApi;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

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
        return hasAnyScopes(scope);
    }

    @Override
    public boolean hasAnyScopes(String... scope) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return CollUtil.containsAny(loginUser.getScopes(), Arrays.asList(scope));
    }
}
