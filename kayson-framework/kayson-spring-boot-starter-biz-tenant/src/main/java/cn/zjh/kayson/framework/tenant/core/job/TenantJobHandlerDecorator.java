package cn.zjh.kayson.framework.tenant.core.job;

import cn.hutool.core.collection.CollUtil;
import cn.zjh.kayson.framework.common.util.json.JsonUtils;
import cn.zjh.kayson.framework.quartz.core.handler.JobHandler;
import cn.zjh.kayson.framework.tenant.core.context.TenantContextHolder;
import cn.zjh.kayson.framework.tenant.core.service.TenantFrameworkService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多租户 JobHandler 装饰器
 * 任务执行时，会按照租户逐个执行 Job 的逻辑
 * 
 * 注意，需要保证 JobHandler 的幂等性。因为 Job 因为某个租户执行失败重试时，之前执行成功的租户也会再次执行。
 * 
 * @author zjh - kayson
 */
@RequiredArgsConstructor
public class TenantJobHandlerDecorator implements JobHandler {

    private final TenantFrameworkService tenantFrameworkService;
    
    /**
     * 被装饰的 Job
     */
    private final JobHandler jobHandler;

    @Override
    public String execute(String param) throws Exception {
        // 获得租户的列表
        List<Long> tenantIds = tenantFrameworkService.getTenantIds();
        if (CollUtil.isEmpty(tenantIds)) {
            return null;
        }
        
        // 逐个租户，执行 Job
        Map<Long, String> results = new ConcurrentHashMap<>();
        tenantIds.parallelStream().forEach(tenantId -> {
            try {
                // 设置租户
                TenantContextHolder.setTenantId(tenantId);
                // 执行Job
                String result = jobHandler.execute(param);
                // 添加结果
                results.put(tenantId, result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                TenantContextHolder.clear();
            }
        });
        return JsonUtils.toJsonString(results);
    }
    
}