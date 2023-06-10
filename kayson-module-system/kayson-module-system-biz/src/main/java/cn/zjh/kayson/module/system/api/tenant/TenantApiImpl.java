package cn.zjh.kayson.module.system.api.tenant;

import java.util.List;

/**
 * 多租户的 API 实现类
 * 
 * @author zjh - kayson
 */
public class TenantApiImpl implements TenantApi{
    
    @Override
    public List<Long> getTenantIdList() {
        return null;
    }

    @Override
    public void validateTenant(Long id) {

    }
    
}
