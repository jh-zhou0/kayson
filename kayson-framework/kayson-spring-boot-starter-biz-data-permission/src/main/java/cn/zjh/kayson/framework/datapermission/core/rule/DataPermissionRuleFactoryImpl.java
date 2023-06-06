package cn.zjh.kayson.framework.datapermission.core.rule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.zjh.kayson.framework.datapermission.core.annotation.DataPermission;
import cn.zjh.kayson.framework.datapermission.core.aop.DataPermissionContextHolder;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认的 DataPermissionRuleFactory 实现类
 * 支持通过 {@link DataPermissionContextHolder} 过滤数据权限
 * 
 * @author zjh - kayson
 */
@RequiredArgsConstructor
public class DataPermissionRuleFactoryImpl implements DataPermissionRuleFactory {
    
    private final List<DataPermissionRule> rules; 
    
    @Override
    public List<DataPermissionRule> getDataPermissionRules() {
        return rules;
    }

    @Override // mappedStatementId 参数，暂时没有用。以后，可以基于 mappedStatementId + DataPermission 进行缓存
    public List<DataPermissionRule> getDataPermissionRule(String mappedStatementId) {
        // 1 未配置数据权限规则，无数据权限
        if (CollUtil.isEmpty(rules)) {
            return Collections.emptyList();
        }
        // 2 未配置 DataPermission 注解，默认开启
        DataPermission dataPermission = DataPermissionContextHolder.get();
        if (dataPermission == null) {
            return rules;
        }
        // 3 已配置 DataPermission 注解，但禁用了，无数据权限
        if (!dataPermission.enable()) {
            return Collections.emptyList();
        }
        // 4 已配置 DataPermission 注解，只选择部分规则
        if (ArrayUtil.isNotEmpty(dataPermission.includeRules())) {
            return rules.stream().filter(rule -> ArrayUtil.contains(dataPermission.includeRules(), rule.getClass()))
                    .collect(Collectors.toList());
        }
        // 5 已配置 DataPermission 注解，只排除部分规则
        if (ArrayUtil.isNotEmpty(dataPermission.excludeRules())) {
            return rules.stream().filter(rule -> !ArrayUtil.contains(dataPermission.excludeRules(), rule.getClass()))
                    .collect(Collectors.toList());
        }
        // 6 已配置，全部规则
        return rules;
    }
}
