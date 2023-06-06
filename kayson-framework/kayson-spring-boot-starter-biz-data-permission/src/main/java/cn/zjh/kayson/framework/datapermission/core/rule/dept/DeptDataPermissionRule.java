package cn.zjh.kayson.framework.datapermission.core.rule.dept;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.zjh.kayson.framework.common.enums.UserTypeEnum;
import cn.zjh.kayson.framework.common.util.collection.CollectionUtils;
import cn.zjh.kayson.framework.common.util.json.JsonUtils;
import cn.zjh.kayson.framework.datapermission.core.rule.DataPermissionRule;
import cn.zjh.kayson.framework.expression.OrExpressionX;
import cn.zjh.kayson.framework.mybatis.core.dataobject.BaseDO;
import cn.zjh.kayson.framework.mybatis.core.util.MyBatisUtils;
import cn.zjh.kayson.framework.security.LoginUser;
import cn.zjh.kayson.framework.security.core.util.SecurityFrameworkUtils;
import cn.zjh.kayson.module.system.api.permission.PermissionApi;
import cn.zjh.kayson.module.system.api.permission.vo.DeptDataPermissionRespDTO;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 基于部门的 {@link DataPermissionRule} 数据权限规则实现
 * 
 * @author zjh - kayson
 */
@AllArgsConstructor
@Slf4j
public class DeptDataPermissionRule implements DataPermissionRule {

    /**
     * LoginUser 的 Context 缓存 Key
     */
    protected static final String CONTEXT_KEY = DeptDataPermissionRule.class.getSimpleName();

    private static final String DEPT_COLUMN_NAME = "dept_id";
    private static final String USER_COLUMN_NAME = "user_id";

    static final Expression EXPRESSION_NULL = new NullValue();
    
    private final PermissionApi permissionApi;

    /**
     * 基于部门的表字段配置
     * 一般情况下，每个表的部门编号字段是 dept_id，通过该配置自定义。
     *
     * key：表名
     * value：字段名
     */
    private final Map<String, String> deptColumns = new HashMap<>();
    /**
     * 基于用户的表字段配置
     * 一般情况下，每个表的部门编号字段是 dept_id，通过该配置自定义。
     *
     * key：表名
     * value：字段名
     */
    private final Map<String, String> userColumns = new HashMap<>();
    /**
     * 所有表名，是 {@link #deptColumns} 和 {@link #userColumns} 的合集
     */
    private final Set<String> TABLE_NAMES = new HashSet<>();
    
    @Override
    public Set<String> getTableNames() {
        return TABLE_NAMES;
    }

    @Override
    public Expression getExpression(String tableName, @Nullable Alias tableAlias) {
        // 只有用户登录的情况下，才进行数据权限的处理
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            return null;
        }
        // 只有管理员用户类型，才进行数据权限的处理
        if (ObjectUtil.notEqual(loginUser.getUserType(), UserTypeEnum.ADMIN.getValue())) {
            return null;
        }
        
        // 获得数据权限
        DeptDataPermissionRespDTO deptDataPermission = loginUser.getContext(CONTEXT_KEY, DeptDataPermissionRespDTO.class);
        // 从上下文中拿不到，则调用逻辑进行获取
        if (deptDataPermission == null) {
            deptDataPermission = permissionApi.getDeptDataPermission(loginUser.getId());
            if (deptDataPermission == null) {
                log.error("[getExpression][LoginUser({}) 获取数据权限为 null]", JsonUtils.toJsonString(loginUser));
                throw new NullPointerException(String.format("LoginUser(%d) Table(%s/%s) 未返回数据权限",
                        loginUser.getId(), tableName, tableAlias != null ? tableAlias.getName() : ""));
            }
            // 添加到上下文中，避免重复计算
            loginUser.setContext(CONTEXT_KEY, deptDataPermission);
        }
        
        // ① 如果是 ALL 可查看全部，则无需拼接条件
        if (deptDataPermission.getAll()) {
            return null;
        }
        // ② 既不能看自己，且不能看部门，100% 无权限
        if (Boolean.FALSE.equals(deptDataPermission.getSelf()) 
                && CollUtil.isEmpty(deptDataPermission.getDeptIds())) {
            return new EqualsTo(null, null); // WHERE null = null，可以保证返回的数据为空
        }
        // ③ 拼接 Dept 和 User 的条件
        Expression deptExpress = buildDeptExpression(tableName, tableAlias, deptDataPermission.getDeptIds());
        Expression userExpression = buildUserExpression(tableName, tableAlias, deptDataPermission.getSelf(), loginUser.getId());
        if (deptExpress == null && userExpression == null) {
            log.warn("[getExpression][LoginUser({}) Table({}/{}) DeptDataPermission({}) 构建的条件为空]", 
                    JsonUtils.toJsonString(loginUser), tableName, tableAlias, JsonUtils.toJsonString(deptDataPermission));
            return EXPRESSION_NULL;
        }
        if (deptExpress == null) {
            return userExpression;
        }
        if (userExpression == null) {
            return deptExpress;
        }
        // 目前，如果有指定部门 + 可查看自己，采用 OR 条件。即，WHERE (dept_id IN ? OR user_id = ?)
        return new OrExpressionX(deptExpress, userExpression);
    }

    private Expression buildDeptExpression(String tableName, Alias tableAlias, Set<Long> deptIds) {
        // 如果不存在配置，则无需作为条件
        String columnName = deptColumns.get(tableName);
        if (StrUtil.isEmpty(columnName)) {
            return null;
        }
        // 如果部门为空，则无条件
        if (CollUtil.isEmpty(deptIds)) {
            return null;
        }
        // 拼接条件
        return new InExpression(MyBatisUtils.buildColumn(tableName, tableAlias, columnName),
                new ExpressionList(CollectionUtils.convertList(deptIds, LongValue::new)));
    }

    private Expression buildUserExpression(String tableName, Alias tableAlias, Boolean self, Long userId) {
        // 如果不查看自己，则无需作为条件
        if (Boolean.FALSE.equals(self)) {
            return null;
        }
        // 如果不存在配置，则无需作为条件
        String columnName = userColumns.get(tableName);
        if (StrUtil.isEmpty(columnName)) {
            return null;
        }
        // 拼接条件
        return new EqualsTo(MyBatisUtils.buildColumn(tableName, tableAlias, columnName), new LongValue(userId));
    }

    // =============添加配置=============
    
    public void addDeptColumn(Class<? extends BaseDO> entityClass) {
        addDeptColumn(entityClass, DEPT_COLUMN_NAME);
    }

    public void addDeptColumn(Class<? extends BaseDO> entityClass, String columnName) {
        String tableName = TableInfoHelper.getTableInfo(entityClass).getTableName();
        addDeptColumn(tableName, columnName);
    }
    
    public void addDeptColumn(String tableName, String columnName) {
        deptColumns.put(tableName, columnName);
        TABLE_NAMES.add(tableName);
    }

    public void addUserColumn(Class<? extends BaseDO> entityClass) {
        addUserColumn(entityClass, USER_COLUMN_NAME);
    }

    public void addUserColumn(Class<? extends BaseDO> entityClass, String columnName) {
        String tableName = TableInfoHelper.getTableInfo(entityClass).getTableName();
        addUserColumn(tableName, columnName);
    }

    public void addUserColumn(String tableName, String columnName) {
        userColumns.put(tableName, columnName);
        TABLE_NAMES.add(tableName);
    }
    
}
