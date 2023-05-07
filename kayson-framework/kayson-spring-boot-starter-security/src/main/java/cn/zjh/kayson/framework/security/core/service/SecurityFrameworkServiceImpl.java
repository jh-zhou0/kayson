package cn.zjh.kayson.framework.security.core.service;

/**
 * 默认的 {@link SecurityFrameworkService} 实现类
 * 
 * @author zjh - kayson
 */
public class SecurityFrameworkServiceImpl implements SecurityFrameworkService{
    
    // TODO
    
    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public boolean hasAnyPermissions(String... permissions) {
        return true;
    }

    @Override
    public boolean hasRole(String role) {
        return true;
    }

    @Override
    public boolean hasAnyRoles(String... roles) {
        return true;
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
