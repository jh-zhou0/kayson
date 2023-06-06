package cn.zjh.kayson.framework.datapermission.config;

import cn.zjh.kayson.framework.datapermission.core.rule.dept.DeptDataPermissionRule;
import cn.zjh.kayson.framework.datapermission.core.rule.dept.DeptDataPermissionRuleCustomizer;
import cn.zjh.kayson.framework.security.LoginUser;
import cn.zjh.kayson.module.system.api.permission.PermissionApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 基于部门的数据权限 AutoConfiguration
 * 
 * @author zjh - kayson
 */
@AutoConfiguration
@ConditionalOnClass(LoginUser.class)
@ConditionalOnBean(value = {PermissionApi.class, DeptDataPermissionRuleCustomizer.class})
public class KaysonDeptDataPermissionAutoConfiguration {
    
    @Bean
    public DeptDataPermissionRule deptDataPermissionRule(PermissionApi permissionApi,
                                                         List<DeptDataPermissionRuleCustomizer> customizers) {
        DeptDataPermissionRule rule = new DeptDataPermissionRule(permissionApi);
        // 设置表配置
        customizers.forEach(customizer -> customizer.customize(rule));
        return rule;
    }
    
}
