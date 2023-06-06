package cn.zjh.kayson.framework.datapermission.core.rule.dept;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.zjh.kayson.framework.common.enums.UserTypeEnum;
import cn.zjh.kayson.framework.common.util.collection.SetUtils;
import cn.zjh.kayson.framework.security.LoginUser;
import cn.zjh.kayson.framework.security.core.util.SecurityFrameworkUtils;
import cn.zjh.kayson.framework.test.core.ut.BaseMockitoUnitTest;
import cn.zjh.kayson.module.system.api.permission.PermissionApi;
import cn.zjh.kayson.module.system.api.permission.vo.DeptDataPermissionRespDTO;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.Map;

import static cn.zjh.kayson.framework.datapermission.core.rule.dept.DeptDataPermissionRule.EXPRESSION_NULL;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.randomPojo;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * {@link DeptDataPermissionRule} 的单元测试
 * 
 * @author zjh - kayson
 */
public class DeptDataPermissionRuleTest extends BaseMockitoUnitTest {
    
    @InjectMocks
    private DeptDataPermissionRule rule;
    
    @Mock
    private PermissionApi permissionApi;

    @BeforeEach // 清空 rule
    @SuppressWarnings("unchecked")
    void setUp() {
        rule.getTableNames().clear();
        Map<String, String> deptColumns = (Map<String, String>) ReflectUtil.getFieldValue(rule, "deptColumns");
        deptColumns.clear();
        Map<String, String> userColumns = (Map<String, String>) ReflectUtil.getFieldValue(rule, "userColumns");
        userColumns.clear();
    }

    @Test // 无 LoginUser
    public void testGetExpression_noLoginUser() {
        // 准备参数
        String tableName = randomString();
        // mock 方法

        // 调用
        Expression expression = rule.getExpression(tableName, null);
        // 断言
        assertNull(expression);
    }

    @Test // 无数据权限时
    public void testGetExpression_noDeptDataPermission() {
        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock
                     = mockStatic(SecurityFrameworkUtils.class)) {
            // 准备参数
            String tableName = "t_user";
            Alias tableAlias = new Alias("u");
            // mock 方法
            LoginUser loginUser = randomPojo(LoginUser.class, o -> o.setId(1L)
                    .setUserType(UserTypeEnum.ADMIN.getValue()));
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUser).thenReturn(loginUser);

            // 调用
            NullPointerException exception = assertThrows(NullPointerException.class,
                    () -> rule.getExpression(tableName, tableAlias));
            // 断言
            assertEquals("LoginUser(1) Table(t_user/u) 未返回数据权限", exception.getMessage());
        }
    }

    @Test // 全部数据权限
    public void testGetExpression_allDeptDataPermission() {
        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock
                     = mockStatic(SecurityFrameworkUtils.class)) {
            // 准备参数
            String tableName = "t_user";
            Alias tableAlias = new Alias("u");
            // mock 方法（LoginUser）
            LoginUser loginUser = randomPojo(LoginUser.class, o -> o.setId(1L)
                    .setUserType(UserTypeEnum.ADMIN.getValue()));
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUser).thenReturn(loginUser);
            // mock 方法（DeptDataPermissionRespDTO）
            DeptDataPermissionRespDTO deptDataPermission = new DeptDataPermissionRespDTO().setAll(true);
            when(permissionApi.getDeptDataPermission(same(1L))).thenReturn(deptDataPermission);

            // 调用
            Expression expression = rule.getExpression(tableName, tableAlias);
            // 断言
            assertNull(expression);
            assertSame(deptDataPermission, loginUser.getContext(DeptDataPermissionRule.CONTEXT_KEY, DeptDataPermissionRespDTO.class));
        }
    }

    @Test // 即不能查看部门，又不能查看自己，则说明 100% 无权限
    public void testGetExpression_noDept_noSelf() {
        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock
                     = mockStatic(SecurityFrameworkUtils.class)) {
            // 准备参数
            String tableName = "t_user";
            Alias tableAlias = new Alias("u");
            // mock 方法（LoginUser）
            LoginUser loginUser = randomPojo(LoginUser.class, o -> o.setId(1L)
                    .setUserType(UserTypeEnum.ADMIN.getValue()));
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUser).thenReturn(loginUser);
            // mock 方法（DeptDataPermissionRespDTO）
            DeptDataPermissionRespDTO deptDataPermission = new DeptDataPermissionRespDTO();
            when(permissionApi.getDeptDataPermission(same(1L))).thenReturn(deptDataPermission);

            // 调用
            Expression expression = rule.getExpression(tableName, tableAlias);
            // 断言
            assertEquals("null = null", expression.toString());
            assertSame(deptDataPermission, loginUser.getContext(DeptDataPermissionRule.CONTEXT_KEY, DeptDataPermissionRespDTO.class));
        }
    }

    @Test // 拼接 Dept 和 User 的条件（字段都不符合）
    public void testGetExpression_noDeptColumn_noSelfColumn() {
        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock
                     = mockStatic(SecurityFrameworkUtils.class)) {
            // 准备参数
            String tableName = "t_user";
            Alias tableAlias = new Alias("u");
            // mock 方法（LoginUser）
            LoginUser loginUser = randomPojo(LoginUser.class, o -> o.setId(1L)
                    .setUserType(UserTypeEnum.ADMIN.getValue()));
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUser).thenReturn(loginUser);
            // mock 方法（DeptDataPermissionRespDTO）
            DeptDataPermissionRespDTO deptDataPermission = new DeptDataPermissionRespDTO()
                    .setDeptIds(SetUtils.asSet(10L, 20L)).setSelf(true);
            when(permissionApi.getDeptDataPermission(same(1L))).thenReturn(deptDataPermission);

            // 调用
            Expression expression = rule.getExpression(tableName, tableAlias);
            // 断言
            assertSame(EXPRESSION_NULL, expression);
            assertSame(deptDataPermission, loginUser.getContext(DeptDataPermissionRule.CONTEXT_KEY, DeptDataPermissionRespDTO.class));
        }
    }

    @Test // 拼接 Dept 和 User 的条件（user 符合）
    public void testGetExpression_noDeptColumn_yesSelfColumn() {
        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock
                     = mockStatic(SecurityFrameworkUtils.class)) {
            // 准备参数
            String tableName = "t_user";
            Alias tableAlias = new Alias("u");
            // mock 方法（LoginUser）
            LoginUser loginUser = randomPojo(LoginUser.class, o -> o.setId(1L)
                    .setUserType(UserTypeEnum.ADMIN.getValue()));
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUser).thenReturn(loginUser);
            // mock 方法（DeptDataPermissionRespDTO）
            DeptDataPermissionRespDTO deptDataPermission = new DeptDataPermissionRespDTO()
                    .setSelf(true);
            when(permissionApi.getDeptDataPermission(same(1L))).thenReturn(deptDataPermission);
            // 添加 user 字段配置
            rule.addUserColumn("t_user", "id");

            // 调用
            Expression expression = rule.getExpression(tableName, tableAlias);
            // 断言
            assertEquals("u.id = 1", expression.toString());
            assertSame(deptDataPermission, loginUser.getContext(DeptDataPermissionRule.CONTEXT_KEY, DeptDataPermissionRespDTO.class));
        }
    }

    @Test // 拼接 Dept 和 User 的条件（dept 符合）
    public void testGetExpression_yesDeptColumn_noSelfColumn() {
        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock
                     = mockStatic(SecurityFrameworkUtils.class)) {
            // 准备参数
            String tableName = "t_user";
            Alias tableAlias = new Alias("u");
            // mock 方法（LoginUser）
            LoginUser loginUser = randomPojo(LoginUser.class, o -> o.setId(1L)
                    .setUserType(UserTypeEnum.ADMIN.getValue()));
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUser).thenReturn(loginUser);
            // mock 方法（DeptDataPermissionRespDTO）
            DeptDataPermissionRespDTO deptDataPermission = new DeptDataPermissionRespDTO()
                    .setDeptIds(CollUtil.newLinkedHashSet(10L, 20L));
            when(permissionApi.getDeptDataPermission(same(1L))).thenReturn(deptDataPermission);
            // 添加 dept 字段配置
            rule.addDeptColumn("t_user", "dept_id");

            // 调用
            Expression expression = rule.getExpression(tableName, tableAlias);
            // 断言
            assertEquals("u.dept_id IN (10, 20)", expression.toString());
            assertSame(deptDataPermission, loginUser.getContext(DeptDataPermissionRule.CONTEXT_KEY, DeptDataPermissionRespDTO.class));
        }
    }

    @Test // 拼接 Dept 和 User 的条件（dept + user 符合）
    public void testGetExpression_yesDeptColumn_yesSelfColumn() {
        try (MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock
                     = mockStatic(SecurityFrameworkUtils.class)) {
            // 准备参数
            String tableName = "t_user";
            Alias tableAlias = new Alias("u");
            // mock 方法（LoginUser）
            LoginUser loginUser = randomPojo(LoginUser.class, o -> o.setId(1L)
                    .setUserType(UserTypeEnum.ADMIN.getValue()));
            securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUser).thenReturn(loginUser);
            // mock 方法（DeptDataPermissionRespDTO）
            DeptDataPermissionRespDTO deptDataPermission = new DeptDataPermissionRespDTO()
                    .setDeptIds(CollUtil.newLinkedHashSet(10L, 20L)).setSelf(true);
            when(permissionApi.getDeptDataPermission(same(1L))).thenReturn(deptDataPermission);
            // 添加 user 字段配置
            rule.addUserColumn("t_user", "id");
            // 添加 dept 字段配置
            rule.addDeptColumn("t_user", "dept_id");

            // 调用
            Expression expression = rule.getExpression(tableName, tableAlias);
            // 断言
            assertEquals("(u.dept_id IN (10, 20) OR u.id = 1)", expression.toString());
            assertSame(deptDataPermission, loginUser.getContext(DeptDataPermissionRule.CONTEXT_KEY, DeptDataPermissionRespDTO.class));
        }
    }
    
}
