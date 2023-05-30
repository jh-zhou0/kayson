package cn.zjh.kayson.module.system.service.permission;

import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.test.core.ut.BaseDbUnitTest;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RoleCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RoleExportReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RoleUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleDO;
import cn.zjh.kayson.module.system.dal.mysql.permission.RoleMapper;
import cn.zjh.kayson.module.system.enums.permission.DataScopeEnum;
import cn.zjh.kayson.module.system.enums.permission.RoleTypeEnum;
import cn.zjh.kayson.module.system.mq.producer.permission.RoleProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.zjh.kayson.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.zjh.kayson.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.zjh.kayson.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.*;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

/**
 * @author zjh - kayson
 */
@Import(RoleServiceImpl.class)
public class RoleServiceImplTest extends BaseDbUnitTest {
    
    @Resource
    private RoleServiceImpl roleService;
    
    @Resource
    private RoleMapper roleMapper;
    
    @MockBean
    private PermissionService permissionService;
    
    @MockBean
    private RoleProducer roleProducer;

    @Test
    void testInitLocalCache() {
        // mock 数据
        RoleDO roleDO1 = randomPojo(RoleDO.class);
        roleMapper.insert(roleDO1);
        RoleDO roleDO2 = randomPojo(RoleDO.class);
        roleMapper.insert(roleDO2);
        
        // 调用
        roleService.initLocalCache();
        // 断言 roleCache 缓存
        Map<Long, RoleDO> roleCache = roleService.getRoleCache();
        assertPojoEquals(roleDO1, roleCache.get(roleDO1.getId()));
        assertPojoEquals(roleDO2, roleCache.get(roleDO2.getId()));
    }

    @Test
    void testCreateRole_success() {
        // 准备参数
        RoleCreateReqVO reqVO = randomPojo(RoleCreateReqVO.class);
        
        // 调用
        Long roleId = roleService.createRole(reqVO, null);
        // 断言
        assertNotNull(roleId);
        RoleDO roleDO = roleMapper.selectById(roleId);
        assertPojoEquals(reqVO, roleDO);
        assertEquals(RoleTypeEnum.CUSTOM.getType(), roleDO.getType());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), roleDO.getStatus());
        assertEquals(DataScopeEnum.ALL.getScope(), roleDO.getDataScope());
        // verify 发送刷新消息
        verify(roleProducer).sendRoleRefreshMessage();
    }

    @Test
    void testUpdateRole_success() {
        // mock 数据
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setType(RoleTypeEnum.CUSTOM.getType()));
        roleMapper.insert(roleDO);
        
        // 准备参数
        RoleUpdateReqVO reqVO = randomPojo(RoleUpdateReqVO.class, o -> o.setId(roleDO.getId()));
        
        // 调用
        roleService.updateRole(reqVO);
        RoleDO dbRoleDO = roleMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, dbRoleDO);
        // verify 发送刷新消息
        verify(roleProducer).sendRoleRefreshMessage();
    }

    @Test
    void testUpdateRoleStatus_success() {
        // mock 数据
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setType(RoleTypeEnum.CUSTOM.getType()));
        roleMapper.insert(roleDO);

        // 准备参数
        Long roleId = roleDO.getId();
        
        // 调用
        roleService.updateRoleStatus(roleId, CommonStatusEnum.DISABLE.getStatus());
        // 断言
        RoleDO role = roleMapper.selectById(roleId);
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), role.getStatus());
        // verify 发送刷新消息
        verify(roleProducer).sendRoleRefreshMessage();
    }

    @Test
    void testDeleteRole_success() {
        // mock 数据
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setType(RoleTypeEnum.CUSTOM.getType()));
        roleMapper.insert(roleDO);
        // 参数准备
        Long id = roleDO.getId();

        // 调用
        roleService.deleteRole(id);
        // 断言
        assertNull(roleMapper.selectById(id));
        // verify 删除相关数据
        verify(permissionService).processRoleDeleted(id);
        // verify 发送刷新消息
        verify(roleProducer).sendRoleRefreshMessage();
    }

    @Test
    void testGetRole() {
        // mock 数据
        RoleDO roleDO = randomPojo(RoleDO.class);
        roleMapper.insert(roleDO);
        // 参数准备
        Long id = roleDO.getId();

        // 调用
        RoleDO dbRoleDO = roleService.getRole(id);
        // 断言
        assertPojoEquals(roleDO, dbRoleDO);
    }

    @Test
    void testGetRoleFromCache() {
        // mock 数据（缓存）
        RoleDO roleDO = randomPojo(RoleDO.class);
        roleMapper.insert(roleDO);
        roleService.initLocalCache();
        // 参数准备
        Long id = roleDO.getId();
        
        // 调用
        RoleDO roleFromCache = roleService.getRoleFromCache(id);
        // 断言
        assertPojoEquals(roleDO, roleFromCache);
    }

    @Test
    void testGetRoleList_withIds() {
        // mock 数据
        RoleDO dbRole = randomPojo(RoleDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        roleMapper.insert(dbRole);
        // 测试 id 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> {}));
        // 准备参数
        Collection<Long> ids = singleton(dbRole.getId());

        // 调用
        List<RoleDO> list = roleService.getRoleList(ids);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbRole, list.get(0));
    }

    @Test
    void testGetRoleListFromCache() {
        // mock 数据
        RoleDO dbRole = randomPojo(RoleDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        roleMapper.insert(dbRole);
        // 测试 id 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> {}));
        roleService.initLocalCache();
        
        // 准备参数
        Set<Long> roleIds = singleton(dbRole.getId());
        
        // 调用
        List<RoleDO> roleListFromCache = roleService.getRoleListFromCache(roleIds);
        //断言
        assertEquals(1, roleListFromCache.size());
        assertPojoEquals(dbRole, roleListFromCache.get(0));
    }

    @Test
    void testGetRoleListByStatus_statusNotEmpty() {
        // mock 数据
        RoleDO dbRole = randomPojo(RoleDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        roleMapper.insert(dbRole);
        // 测试 status 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        
        // 调用
        List<RoleDO> roleListByStatus = roleService.getRoleListByStatus(singleton(CommonStatusEnum.ENABLE.getStatus()));
        // 断言
        assertEquals(1, roleListByStatus.size());
        assertPojoEquals(dbRole, roleListByStatus.get(0));
    }

    @Test
    void testGetRolePage() {
        // mock 数据
        RoleDO dbRole = randomPojo(RoleDO.class, o -> { // 等会查询到
            o.setName("kayson");
            o.setCode("admin");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(buildTime(2023, 5, 22));
        });
        roleMapper.insert(dbRole);
        // 测试 name 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> o.setName("zjh")));
        // 测试 code 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> o.setCode("zjh")));
        // 测试 createTime 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> o.setCreateTime(buildTime(2023, 5, 1))));
        // 准备参数
        RolePageReqVO reqVO = new RolePageReqVO();
        reqVO.setName("k");
        reqVO.setCode("admin");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime(buildBetweenTime(2023, 5, 21, 2023, 5, 23));

        // 调用
        PageResult<RoleDO> pageResult = roleService.getRolePage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbRole, pageResult.getList().get(0));
    }

    @Test
    public void testGetRoleList() {
        // mock 数据
        RoleDO dbRole = randomPojo(RoleDO.class, o -> { // 等会查询到
            o.setName("kayson");
            o.setCode("kayson");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(buildTime(2023, 5, 30));
        });
        roleMapper.insert(dbRole);
        // 测试 name 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> o.setName("红薯")));
        // 测试 code 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> o.setCode("hong")));
        // 测试 createTime 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> o.setCreateTime(buildTime(2023, 5, 16))));
        // 准备参数
        RoleExportReqVO reqVO = new RoleExportReqVO();
        reqVO.setName("kay");
        reqVO.setCode("kay");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime(buildBetweenTime(2023, 5, 29, 2023, 5, 31));

        // 调用
        List<RoleDO> list = roleService.getRoleList(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbRole, list.get(0));
    }

    @Test
    void testHasAnySuperAdmin() {
        // 是超级
        assertTrue(roleService.hasAnySuperAdmin(singletonList(randomPojo(RoleDO.class,
                o -> o.setCode("super_admin")))));
        // 非超级
        assertFalse(roleService.hasAnySuperAdmin(singletonList(randomPojo(RoleDO.class,
                o -> o.setCode("tenant_admin")))));
    }

    @Test
    void testValidateRoleDuplicate_success() {
        // 调用，不会抛异常
        roleService.validateRoleDuplicate(null, randomString(), randomString());
    }

    @Test
    void testValidateRoleDuplicate_nameDuplicate() {
        // mock 数据
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setName("role_name"));
        roleMapper.insert(roleDO);
        // 准备参数
        String name = "role_name";

        // 调用，并断言异常
        assertServiceException(() -> roleService.validateRoleDuplicate(null, name, randomString()),
                ROLE_NAME_DUPLICATE, name);
    }

    @Test
    public void testValidateRoleDuplicate_codeDuplicate() {
        // mock 数据
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setCode("code"));
        roleMapper.insert(roleDO);
        // 准备参数
        String code = "code";

        // 调用，并断言异常
        assertServiceException(() -> roleService.validateRoleDuplicate(null, randomString(), code),
                ROLE_CODE_DUPLICATE, code);
    }

    @Test
    void testValidateUpdateRole_success() {
        RoleDO roleDO = randomPojo(RoleDO.class);
        roleMapper.insert(roleDO);
        // 准备参数
        Long id = roleDO.getId();

        // 调用，无异常
        roleService.validateRoleForUpdate(id);
    }

    @Test
    void testValidateUpdateRole_roleIdNotExist() {
        assertServiceException(() -> roleService.validateRoleForUpdate(randomLong()), ROLE_NOT_EXISTS);
    }

    @Test
    void testValidateUpdateRole_systemRoleCanNotBeUpdate() {
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setType(RoleTypeEnum.SYSTEM.getType()));
        roleMapper.insert(roleDO);
        // 准备参数
        Long id = roleDO.getId();

        assertServiceException(() -> roleService.validateRoleForUpdate(id), ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE);
    }
}
