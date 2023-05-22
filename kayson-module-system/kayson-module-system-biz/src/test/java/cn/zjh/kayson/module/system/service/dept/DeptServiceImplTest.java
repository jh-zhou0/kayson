package cn.zjh.kayson.module.system.service.dept;

import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.util.object.ObjectUtils;
import cn.zjh.kayson.framework.test.core.ut.BaseDbUnitTest;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.dept.DeptCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.dept.DeptUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.dept.DeptDO;
import cn.zjh.kayson.module.system.dal.mysql.dept.DeptMapper;
import cn.zjh.kayson.module.system.enums.dept.DeptIdEnum;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.*;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author zjh - kayson
 */
@Import(DeptServiceImpl.class) // 导入 DeptServiceImpl Bean
public class DeptServiceImplTest extends BaseDbUnitTest {
    
    @Resource // 注入要测试的 AdminAuthServiceImpl Bean
    private DeptServiceImpl deptService;
    
    @Resource // 注入内部的 DeptMapper Bean
    private DeptMapper deptMapper;


    @Test
    void testGetDeptList() {
        // mock 数据
        DeptDO dept = randomPojo(DeptDO.class, o -> o.setName("开发部").setStatus(CommonStatusEnum.ENABLE.getStatus()));
        deptMapper.insert(dept);
        deptMapper.insert(ObjectUtils.cloneIgnoreId(dept, o -> o.setName("发")));
        deptMapper.insert(ObjectUtils.cloneIgnoreId(dept, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        
        // 准备参数
        DeptListReqVO reqVO = new DeptListReqVO().setName("开").setStatus(CommonStatusEnum.ENABLE.getStatus());
        
        // 调用
        List<DeptDO> deptList = deptService.getDeptList(reqVO);
        // 断言
        assertEquals(1, deptList.size());
        assertPojoEquals(dept, deptList.get(0));
    }

    @Test
    void testCreateDept_success() {
        // 准备参数
        DeptCreateReqVO reqVO = randomPojo(DeptCreateReqVO.class, o -> {
            o.setParentId(DeptIdEnum.ROOT.getId());
            o.setStatus(randomCommonStatus());
        });

        // 调用
        Long deptId = deptService.createDept(reqVO);
        // 断言
        assertNotNull(deptId);
        // 校验记录的属性是否正确
        DeptDO dept = deptMapper.selectById(deptId);
        assertPojoEquals(reqVO, dept);
    }

    @Test
    void testUpdateDept_success() {
        // mock 数据
        DeptDO deptDO = randomPojo(DeptDO.class, o -> o.setStatus(randomCommonStatus()));
        deptMapper.insert(deptDO); // @Sql: 先插入出一条存在的数据
        // 准备参数
        DeptUpdateReqVO reqVO = randomPojo(DeptUpdateReqVO.class, o -> 
                o.setId(deptDO.getId()).setParentId(DeptIdEnum.ROOT.getId()).setStatus(randomCommonStatus()));
        
        // 调用
        deptService.updateDept(reqVO);
        // 校验是否更新正确
        DeptDO dept = deptMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, dept);
    }

    @Test
    void testDeleteDept_success() {
        // mock 数据
        DeptDO deptDO = randomPojo(DeptDO.class, o -> o.setStatus(randomCommonStatus()));
        deptMapper.insert(deptDO); // @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = deptDO.getId();
        
        // 调用
        deptService.deleteDept(id);
        // 校验数据不存在了
        assertNull(deptMapper.selectById(id));
    }

    @Test
    void testValidateDept_parentNotExitsForCreate() {
        // 准备参数
        DeptCreateReqVO reqVO = randomPojo(DeptCreateReqVO.class,
                o -> o.setStatus(randomCommonStatus()));

        // 调用,并断言异常
        assertServiceException(() -> deptService.createDept(reqVO), DEPT_PARENT_NOT_EXITS);
    }

    @Test
    void testValidateDept_notEnableForCreate() {
        // mock 数据
        DeptDO deptDO = randomPojo(DeptDO.class, o ->o.setStatus(CommonStatusEnum.DISABLE.getStatus()));
        deptMapper.insert(deptDO); // @Sql: 先插入出一条存在的数据
        // 准备参数
        DeptCreateReqVO reqVO = randomPojo(DeptCreateReqVO.class, o ->
                o.setParentId(deptDO.getId()));
        
        // 调用, 并断言异常
        assertServiceException(() -> deptService.createDept(reqVO), DEPT_NOT_ENABLE);
    }

    @Test
    void testValidateDept_parentErrorForUpdate() {
        // mock 数据
        DeptDO deptDO = randomPojo(DeptDO.class, o ->
                o.setParentId(DeptIdEnum.ROOT.getId()).setStatus(randomCommonStatus()));
        deptMapper.insert(deptDO); // @Sql: 先插入出一条存在的数据
        // 准备参数
        DeptUpdateReqVO reqVO = randomPojo(DeptUpdateReqVO.class, o ->
                o.setId(deptDO.getId()).setParentId(deptDO.getId())); // 设置自己为父部门

        // 调用, 并断言异常
        assertServiceException(() -> deptService.updateDept(reqVO), DEPT_PARENT_ERROR);
    }

    @Test
    void testValidateDept_parentIsChildForUpdate() {
        // mock 数据
        DeptDO parentDept = randomPojo(DeptDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        deptMapper.insert(parentDept);
        DeptDO childDept = randomPojo(DeptDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setParentId(parentDept.getId());
        });
        deptMapper.insert(childDept);
        
        // 准备参数
        DeptUpdateReqVO reqVO = randomPojo(DeptUpdateReqVO.class, o -> {
            // 设置自己的子部门为父部门
            o.setParentId(childDept.getId());
            // 设置更新的 ID
            o.setId(parentDept.getId());
        });

        // 调用, 并断言异常
        assertServiceException(() -> deptService.updateDept(reqVO), DEPT_PARENT_IS_CHILD);
    }

    @Test
    void testValidateDept_nameDuplicateForUpdate() {
        // mock 数据
        DeptDO deptDO = randomPojo(DeptDO.class, o -> 
                o.setParentId(DeptIdEnum.ROOT.getId()).setStatus(randomCommonStatus()));
        deptMapper.insert(deptDO); // @Sql: 先插入出一条存在的数据
        DeptDO nameDeptDO = randomPojo(DeptDO.class, o -> 
                o.setParentId(DeptIdEnum.ROOT.getId()).setStatus(randomCommonStatus()));
        deptMapper.insert(nameDeptDO);
        // 准备参数
        DeptUpdateReqVO reqVO = randomPojo(DeptUpdateReqVO.class, o -> 
                o.setId(deptDO.getId()).setParentId(DeptIdEnum.ROOT.getId()).setName(nameDeptDO.getName()));

        // 调用, 并断言异常
        assertServiceException(() -> deptService.updateDept(reqVO), DEPT_NAME_DUPLICATE);
    }

    @Test
    void testValidateDept_notFoundForDelete() {
        // 准备参数
        Long id = randomLong();

        // 调用, 并断言异常
        assertServiceException(() -> deptService.deleteDept(id), DEPT_NOT_FOUND);
    }

    @Test
    void testValidateDept_exitsChildrenForDelete() {
        // mock 数据
        DeptDO parentDept = randomPojo(DeptDO.class, o -> o.setStatus(randomCommonStatus()));
        deptMapper.insert(parentDept);// @Sql: 先插入出一条存在的数据
        // 准备参数
        DeptDO childrenDeptDO = randomPojo(DeptDO.class, o -> {
            o.setParentId(parentDept.getId());
            o.setStatus(randomCommonStatus());
        });
        // 插入子部门
        deptMapper.insert(childrenDeptDO);

        // 调用, 并断言异常
        assertServiceException(() -> deptService.deleteDept(parentDept.getId()), DEPT_EXITS_CHILDREN);
    }

    @Test
    void testGetDept() {
        // mock 数据
        DeptDO deptDO = randomPojo(DeptDO.class, o -> o.setStatus(randomCommonStatus()));
        deptMapper.insert(deptDO);
        // 准备参数
        Long id = deptDO.getId();
        
        // 调用
        DeptDO dept = deptService.getDept(id);
        // 断言
        assertEquals(deptDO, dept);
    }

    @Test
    void testGetDeptMap() {
        // mock 数据
        DeptDO dept01 = randomPojo(DeptDO.class, o -> o.setStatus(randomCommonStatus()));
        DeptDO dept02 = randomPojo(DeptDO.class, o -> o.setStatus(randomCommonStatus()));
        deptMapper.insert(dept01);
        deptMapper.insert(dept02);
        // 准备参数
        List<Long> ids = Arrays.asList(dept01.getId(), dept02.getId());

        // 调用
        Map<Long, DeptDO> deptMap = deptService.getDeptMap(ids);
        // 断言
        assertEquals(2, deptMap.size());
        assertEquals(dept01, deptMap.get(dept01.getId()));
        assertEquals(dept02, deptMap.get(dept02.getId()));
    }
    
}
