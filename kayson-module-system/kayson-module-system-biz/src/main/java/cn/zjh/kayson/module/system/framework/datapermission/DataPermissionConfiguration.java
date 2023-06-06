package cn.zjh.kayson.module.system.framework.datapermission;

import cn.zjh.kayson.framework.datapermission.core.rule.dept.DeptDataPermissionRuleCustomizer;
import cn.zjh.kayson.module.system.dal.dataobject.dept.DeptDO;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * system 模块的数据权限 Configuration
 * 
 * @author zjh - kayson
 */
@Configuration(proxyBeanMethods = false)
public class DataPermissionConfiguration {
    
    @Bean
    public DeptDataPermissionRuleCustomizer deptDataPermissionRuleCustomizer() {
        return rule -> {
            // dept
            rule.addDeptColumn(AdminUserDO.class);
            rule.addDeptColumn(DeptDO.class, "id");
            // user
            rule.addUserColumn(AdminUserDO.class, "id");
        };
    }
    
}
