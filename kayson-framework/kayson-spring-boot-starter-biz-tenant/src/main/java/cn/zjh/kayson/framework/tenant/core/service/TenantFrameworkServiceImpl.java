package cn.zjh.kayson.framework.tenant.core.service;

import cn.zjh.kayson.framework.common.exception.ServiceException;
import cn.zjh.kayson.framework.common.util.cache.CacheUtils;
import cn.zjh.kayson.module.system.api.tenant.TenantApi;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.List;

/**
 * Tenant 框架 Service 实现类
 * 
 * @author zjh - kayson
 */
@RequiredArgsConstructor
public class TenantFrameworkServiceImpl implements TenantFrameworkService {
    
    private static final ServiceException SERVICE_EXCEPTION_NULL = new ServiceException();
    
    private final TenantApi tenantApi;

    /**
     * 针对 {@link #getTenantIds()} 的缓存
     */
    private final LoadingCache<Object, List<Long>> getTenantIdsCache = CacheUtils.buildAsyncReloadingCache(
            Duration.ofMinutes(1L), // 过期时间 1 分钟
            new CacheLoader<Object, List<Long>>() {
                @Override
                public List<Long> load(Object key) {
                    return tenantApi.getTenantIdList();
                }
            });

    /**
     * 针对 {@link #validateTenant(Long)} 的缓存
     */
    private final LoadingCache<Long, ServiceException> validTenantCache = CacheUtils.buildAsyncReloadingCache(
            Duration.ofMinutes(1L), // 过期时间 1 分钟
            new CacheLoader<Long, ServiceException>() {
                @Override
                public ServiceException load(Long id) {
                    try {
                        tenantApi.validateTenant(id);
                        return SERVICE_EXCEPTION_NULL;
                    } catch (ServiceException e) {
                        return e;
                    }
                }
            });
    
    
    @Override
    @SneakyThrows
    public List<Long> getTenantIds() {
        return getTenantIdsCache.get(Boolean.TRUE);
    }

    @Override
    public void validateTenant(Long id) {
        ServiceException exception = validTenantCache.getUnchecked(id);
        if (exception != SERVICE_EXCEPTION_NULL) {
            throw exception;
        }
    }
    
}
