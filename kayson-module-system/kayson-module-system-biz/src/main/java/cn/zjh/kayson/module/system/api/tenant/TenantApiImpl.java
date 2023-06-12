package cn.zjh.kayson.module.system.api.tenant;

import cn.zjh.kayson.module.system.service.tenant.TenantService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 多租户的 API 实现类
 * 
 * @author zjh - kayson
 */
@Service
public class TenantApiImpl implements TenantApi{
    
    @Resource
    private TenantService tenantService;
    
    @Override
    public List<Long> getTenantIdList() {
        return tenantService.getTenantIdList();
    }

    @Override
    public void validateTenant(Long id) {
        tenantService.validateTenant(id);
    }
    
}
