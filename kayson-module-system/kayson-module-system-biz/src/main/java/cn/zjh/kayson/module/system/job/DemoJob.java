package cn.zjh.kayson.module.system.job;

import cn.zjh.kayson.framework.quartz.core.handler.JobHandler;
import cn.zjh.kayson.framework.tenant.core.context.TenantContextHolder;
import cn.zjh.kayson.framework.tenant.core.job.TenantJob;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import cn.zjh.kayson.module.system.dal.mysql.user.AdminUserMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zjh - kayson
 */
@Component
@TenantJob // 标记多租户
public class DemoJob implements JobHandler {

    @Resource
    private AdminUserMapper adminUserMapper;

    @Override
    public String execute(String param) {
        System.out.println("当前租户：" + TenantContextHolder.getTenantId());
        List<AdminUserDO> users = adminUserMapper.selectList();
        return "用户数量：" + users.size();
    }

}
