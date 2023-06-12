package cn.zjh.kayson.module.system.service.tenant;

import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.test.core.ut.BaseDbUnitTest;
import cn.zjh.kayson.module.system.controller.admin.tenant.vo.packages.TenantPackageCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.tenant.vo.packages.TenantPackagePageReqVO;
import cn.zjh.kayson.module.system.controller.admin.tenant.vo.packages.TenantPackageUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.tenant.TenantDO;
import cn.zjh.kayson.module.system.dal.dataobject.tenant.TenantPackageDO;
import cn.zjh.kayson.module.system.dal.mysql.tenant.TenantPackageMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static cn.zjh.kayson.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.zjh.kayson.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.zjh.kayson.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.randomLong;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.randomPojo;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author zjh - kayson
 */
@Import(TenantPackageServiceImpl.class)
public class TenantPackageServiceImplTest extends BaseDbUnitTest {
    
    @Resource
    private TenantPackageServiceImpl tenantPackageService;
    
    @Resource
    private TenantPackageMapper tenantPackageMapper;
    
    @MockBean
    private TenantService tenantService;

    @Test
    void testCreateTenantPackage_success() {
        // 准备参数
        TenantPackageCreateReqVO reqVO = randomPojo(TenantPackageCreateReqVO.class);
        
        // 调用
        Long tenantPackageId = tenantPackageService.createTenantPackage(reqVO);
        // 断言
        assertNotNull(tenantPackageId);
        // 校验记录的属性是否正确
        TenantPackageDO tenantPackage = tenantPackageMapper.selectById(tenantPackageId);
        assertPojoEquals(reqVO, tenantPackage);
    }


    @Test
    void testUpdateTenantPackage_success() {
        // mock 数据
        TenantPackageDO tenantPackageDO = randomPojo(TenantPackageDO.class);
        tenantPackageMapper.insert(tenantPackageDO);
        // 准备参数
        TenantPackageUpdateReqVO reqVO = randomPojo(TenantPackageUpdateReqVO.class,
                o -> o.setId(tenantPackageDO.getId()));
        // mock 方法
        Long tenantId01 = randomLong();
        Long tenantId02 = randomLong();
        when(tenantService.getTenantListByPackageId(eq(tenantPackageDO.getId()))).thenReturn(Arrays.asList(
                randomPojo(TenantDO.class, o -> o.setId(tenantId01)),
                randomPojo(TenantDO.class, o -> o.setId(tenantId02))
        ));
        
        // 调用
        tenantPackageService.updateTenantPackage(reqVO);
        // 断言
        TenantPackageDO tenantPackage = tenantPackageMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, tenantPackage);
        // 校验调用租户的菜单
        verify(tenantService).updateTenantRoleMenu(eq(tenantId01), eq(reqVO.getMenuIds()));
        verify(tenantService).updateTenantRoleMenu(eq(tenantId02), eq(reqVO.getMenuIds()));
    }

    @Test
    public void testUpdateTenantPackage_notExists() {
        // 准备参数
        TenantPackageUpdateReqVO reqVO = randomPojo(TenantPackageUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> tenantPackageService.updateTenantPackage(reqVO), TENANT_PACKAGE_NOT_EXISTS);
    }

    @Test
    public void testDeleteTenantPackage_success() {
        // mock 数据
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class);
        tenantPackageMapper.insert(dbTenantPackage);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbTenantPackage.getId();
        // mock 租户未使用该套餐
        when(tenantService.getTenantCountByPackageId(eq(id))).thenReturn(0L);

        // 调用
        tenantPackageService.deleteTenantPackage(id);
        // 校验数据不存在了
        assertNull(tenantPackageMapper.selectById(id));
    }

    @Test
    public void testDeleteTenantPackage_notExists() {
        // 准备参数
        Long id = randomLong();

        // 调用, 并断言异常
        assertServiceException(() -> tenantPackageService.deleteTenantPackage(id), TENANT_PACKAGE_NOT_EXISTS);
    }

    @Test
    public void testDeleteTenantPackage_used() {
        // mock 数据
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class);
        tenantPackageMapper.insert(dbTenantPackage);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbTenantPackage.getId();
        // mock 租户在使用该套餐
        when(tenantService.getTenantCountByPackageId(eq(id))).thenReturn(1L);

        // 调用, 并断言异常
        assertServiceException(() -> tenantPackageService.deleteTenantPackage(id), TENANT_PACKAGE_USED);
    }

    @Test
    public void testGetTenantPackagePage() {
        // mock 数据
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class, o -> { // 等会查询到
            o.setName("普通套餐");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setRemark("common");
            o.setCreateTime(buildTime(2023, 6, 12));
        });
        tenantPackageMapper.insert(dbTenantPackage);
        // 测试 name 不匹配
        tenantPackageMapper.insert(cloneIgnoreId(dbTenantPackage, o -> o.setName("套餐")));
        // 测试 status 不匹配
        tenantPackageMapper.insert(cloneIgnoreId(dbTenantPackage, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 测试 remark 不匹配
        tenantPackageMapper.insert(cloneIgnoreId(dbTenantPackage, o -> o.setRemark("vip")));
        // 测试 createTime 不匹配
        tenantPackageMapper.insert(cloneIgnoreId(dbTenantPackage, o -> o.setCreateTime(buildTime(2023, 11, 11))));
        // 准备参数
        TenantPackagePageReqVO reqVO = new TenantPackagePageReqVO();
        reqVO.setName("普通");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setRemark("com");
        reqVO.setCreateTime(buildBetweenTime(2023, 6, 9, 2023, 6, 15));

        // 调用
        PageResult<TenantPackageDO> pageResult = tenantPackageService.getTenantPackagePage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbTenantPackage, pageResult.getList().get(0));
    }

    @Test
    public void testValidTenantPackage_success() {
        // mock 数据
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class,
                o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        tenantPackageMapper.insert(dbTenantPackage);// @Sql: 先插入出一条存在的数据

        // 调用
        TenantPackageDO result = tenantPackageService.validateTenantPackage(dbTenantPackage.getId());
        // 断言
        assertPojoEquals(dbTenantPackage, result);
    }

    @Test
    public void testValidTenantPackage_notExists() {
        // 准备参数
        Long id = randomLong();

        // 调用, 并断言异常
        assertServiceException(() -> tenantPackageService.validateTenantPackage(id), TENANT_PACKAGE_NOT_EXISTS);
    }

    @Test
    public void testValidTenantPackage_disable() {
        // mock 数据
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class,
                o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus()));
        tenantPackageMapper.insert(dbTenantPackage);// @Sql: 先插入出一条存在的数据

        // 调用, 并断言异常
        assertServiceException(() -> tenantPackageService.validateTenantPackage(dbTenantPackage.getId()),
                TENANT_PACKAGE_DISABLE, dbTenantPackage.getName());
    }

    @Test
    public void testGetTenantPackage() {
        // mock 数据
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class);
        tenantPackageMapper.insert(dbTenantPackage);// @Sql: 先插入出一条存在的数据

        // 调用
        TenantPackageDO result = tenantPackageService.getTenantPackage(dbTenantPackage.getId());
        // 断言
        assertPojoEquals(result, dbTenantPackage);
    }

    @Test
    public void testGetTenantPackageListByStatus() {
        // mock 数据
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class,
                o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        tenantPackageMapper.insert(dbTenantPackage);
        // 测试 status 不匹配
        tenantPackageMapper.insert(cloneIgnoreId(dbTenantPackage,
                o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));

        // 调用
        List<TenantPackageDO> list = tenantPackageService.getTenantPackageListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        assertEquals(1, list.size());
        assertPojoEquals(dbTenantPackage, list.get(0));
    }

}
