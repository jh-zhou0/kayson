package cn.zjh.kayson.module.system.service.dict;

import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.test.core.ut.BaseDbUnitTest;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.DictDataCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.DictDataPageReqVO;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.DictDataUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.dict.DictDataDO;
import cn.zjh.kayson.module.system.dal.dataobject.dict.DictTypeDO;
import cn.zjh.kayson.module.system.dal.mysql.dict.DictDataMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static cn.zjh.kayson.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.*;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author zjh - kayson
 */
@Import(DictDataServiceImpl.class)
public class DictDataServiceImplTest extends BaseDbUnitTest {
    
    @Resource
    private DictDataServiceImpl dictDataService;
    
    @Resource
    private DictDataMapper dictDataMapper;
    
    @MockBean
    private DictTypeService dictTypeService;

    @Test
    void testCreateDictData_success() {
        // 准备参数
        DictDataCreateReqVO reqVO = randomPojo(DictDataCreateReqVO.class, o -> o.setStatus(randomCommonStatus()));
        // mock 方法
        DictTypeDO dictDataDO = randomPojo(DictTypeDO.class,
                o -> o.setType(reqVO.getDictType()).setStatus(randomCommonStatus()));
        when(dictTypeService.getDictType(eq(reqVO.getDictType()))).thenReturn(dictDataDO);
        
        // 调用
        Long id = dictDataService.createDictData(reqVO);
        // 断言
        assertNotNull(id);
        // 校验记录的属性是否正确
        DictDataDO dictData = dictDataMapper.selectById(id);
        assertPojoEquals(reqVO, dictData);
    }

    @Test
    public void testUpdateDictData_success() {
        // mock 数据
        DictDataDO dbDictData = randomPojo(DictDataDO.class, o -> o.setStatus(randomCommonStatus()));
        dictDataMapper.insert(dbDictData);// @Sql: 先插入出一条存在的数据
        // 准备参数
        DictDataUpdateReqVO reqVO = randomPojo(DictDataUpdateReqVO.class, o -> {
            o.setId(dbDictData.getId()); // 设置更新的 ID
            o.setStatus(randomCommonStatus());
        });
        // mock 方法，字典类型
        DictTypeDO dictDataDO = randomPojo(DictTypeDO.class,
                o -> o.setType(reqVO.getDictType()).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(dictTypeService.getDictType(eq(reqVO.getDictType()))).thenReturn(dictDataDO);

        // 调用
        dictDataService.updateDictData(reqVO);
        // 校验是否更新正确
        DictDataDO dictData = dictDataMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, dictData);
    }

    @Test
    public void testDeleteDictData_success() {
        // mock 数据
        DictDataDO dbDictData = randomPojo(DictDataDO.class, o -> o.setStatus(randomCommonStatus()));
        dictDataMapper.insert(dbDictData);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDictData.getId();

        // 调用
        dictDataService.deleteDictData(id);
        // 校验数据不存在了
        assertNull(dictDataMapper.selectById(id));
    }

    @Test
    public void testGetDictDataList() {
        // mock 数据
        DictDataDO dictDataDO01 = randomPojo(DictDataDO.class, o ->
                o.setStatus(randomCommonStatus()).setDictType("kayson").setSort(2));
        dictDataMapper.insert(dictDataDO01);
        DictDataDO dictDataDO02 = randomPojo(DictDataDO.class, o ->
                o.setStatus(randomCommonStatus()).setDictType("kayson").setSort(1));
        dictDataMapper.insert(dictDataDO02);
        // 准备参数

        // 调用
        List<DictDataDO> dictDataDOList = dictDataService.getDictDataList();
        // 断言
        assertEquals(2, dictDataDOList.size());
        assertPojoEquals(dictDataDO02, dictDataDOList.get(0));
        assertPojoEquals(dictDataDO01, dictDataDOList.get(1));
    }

    @Test
    public void testGetDictDataPage() {
        // mock 数据
        DictDataDO dbDictData = randomPojo(DictDataDO.class, o -> { // 等会查询到
            o.setLabel("kayson");
            o.setDictType("sys_kayson");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        dictDataMapper.insert(dbDictData);
        // 测试 label 不匹配
        dictDataMapper.insert(cloneIgnoreId(dbDictData, o -> o.setLabel("label")));
        // 测试 dictType 不匹配
        dictDataMapper.insert(cloneIgnoreId(dbDictData, o -> o.setDictType("type")));
        // 测试 status 不匹配
        dictDataMapper.insert(cloneIgnoreId(dbDictData, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 准备参数
        DictDataPageReqVO reqVO = new DictDataPageReqVO();
        reqVO.setLabel("kay");
        reqVO.setDictType("sys_kayson");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        PageResult<DictDataDO> pageResult = dictDataService.getDictDataPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbDictData, pageResult.getList().get(0));
    }

    @Test
    public void testGetDictData() {
        // mock 数据
        DictDataDO dbDictData = randomPojo(DictDataDO.class, o -> o.setStatus(randomCommonStatus()));
        dictDataMapper.insert(dbDictData);
        // 准备参数
        Long id = dbDictData.getId();

        // 调用
        DictDataDO dictData = dictDataService.getDictData(id);
        // 断言
        assertPojoEquals(dbDictData, dictData);
    }

    @Test
    public void testCountByDictType() {
        // mock 数据
        dictDataMapper.insert(randomPojo(DictDataDO.class, o -> o.setStatus(randomCommonStatus()).setDictType("kayson")));
        dictDataMapper.insert(randomPojo(DictDataDO.class, o -> o.setStatus(randomCommonStatus()).setDictType("kayson")));
        dictDataMapper.insert(randomPojo(DictDataDO.class, o -> o.setStatus(randomCommonStatus()).setDictType("kay")));
        // 准备参数
        String dictType = "kayson";

        // 调用
        long count = dictDataService.countByDictType(dictType);
        // 校验
        assertEquals(2L, count);
    }

    @Test
    public void testGetDictData_dictType() {
        // mock 数据
        DictDataDO dictDataDO = randomPojo(DictDataDO.class,
                o -> o.setStatus(randomCommonStatus()).setDictType("kayson").setValue("1"));
        dictDataMapper.insert(dictDataDO);
        DictDataDO dictDataDO02 = randomPojo(DictDataDO.class,
                o -> o.setStatus(randomCommonStatus()).setDictType("kayson").setValue("2"));
        dictDataMapper.insert(dictDataDO02);
        // 准备参数
        String dictType = "kayson";
        String value = "1";

        // 调用
        DictDataDO dbDictData = dictDataService.getDictData(dictType, value);
        // 断言
        assertEquals(dictDataDO, dbDictData);
    }

    @Test
    public void testParseDictData() {
        // mock 数据
        DictDataDO dictDataDO = randomPojo(DictDataDO.class,
                o -> o.setStatus(randomCommonStatus()).setDictType("kayson").setLabel("1"));
        dictDataMapper.insert(dictDataDO);
        DictDataDO dictDataDO02 = randomPojo(DictDataDO.class,
                o -> o.setStatus(randomCommonStatus()).setDictType("kayson").setValue("2"));
        dictDataMapper.insert(dictDataDO02);
        // 准备参数
        String dictType = "kayson";
        String label = "1";

        // 调用
        DictDataDO dbDictData = dictDataService.parseDictData(dictType, label);
        // 断言
        assertEquals(dictDataDO, dbDictData);
    }

    @Test
    public void testValidateDictDataExists_success() {
        // mock 数据
        DictDataDO dbDictData = randomPojo(DictDataDO.class, o -> o.setStatus(randomCommonStatus()));
        dictDataMapper.insert(dbDictData);// @Sql: 先插入出一条存在的数据

        // 调用成功
        dictDataService.validateDictDataExists(dbDictData.getId());
    }

    @Test
    public void testValidateDictDataExists_notExists() {
        assertServiceException(() -> dictDataService.validateDictDataExists(randomLong()), DICT_DATA_NOT_EXISTS);
    }

    @Test
    public void testValidateDictTypeExists_success() {
        // mock 方法，数据类型被禁用
        String type = randomString();
        DictTypeDO dictDataDO = randomPojo(DictTypeDO.class,
                o -> o.setType(type).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(dictTypeService.getDictType(eq(type))).thenReturn(dictDataDO);

        // 调用, 成功
        dictDataService.validateDictTypeExists(type);
    }

    @Test
    public void testValidateDictTypeExists_notExists() {
        assertServiceException(() -> dictDataService.validateDictTypeExists(randomString()), DICT_TYPE_NOT_EXISTS);
    }

    @Test
    public void testValidateDictTypeExists_notEnable() {
        // mock 方法，数据类型被禁用
        String dictType = randomString();
        when(dictTypeService.getDictType(eq(dictType))).thenReturn(
                randomPojo(DictTypeDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));

        // 调用, 并断言异常
        assertServiceException(() -> dictDataService.validateDictTypeExists(dictType), DICT_TYPE_NOT_ENABLE);
    }

    @Test
    public void testValidateDictDataValueUnique_success() {
        // 调用，成功
        dictDataService.validateDictDataValueUnique(randomLong(), randomString(), randomString());
    }

    @Test
    public void testValidateDictDataValueUnique_valueDuplicateForCreate() {
        // 准备参数
        String dictType = randomString();
        String value = randomString();
        // mock 数据
        dictDataMapper.insert(randomPojo(DictDataDO.class, o -> {
            o.setStatus(randomCommonStatus());
            o.setDictType(dictType);
            o.setValue(value);
        }));

        // 调用，校验异常
        assertServiceException(() -> dictDataService.validateDictDataValueUnique(null, dictType, value),
                DICT_DATA_VALUE_DUPLICATE);
    }

    @Test
    public void testValidateDictDataValueUnique_valueDuplicateForUpdate() {
        // 准备参数
        Long id = randomLong();
        String dictType = randomString();
        String value = randomString();
        // mock 数据
        dictDataMapper.insert(randomPojo(DictDataDO.class, o -> {
            o.setStatus(randomCommonStatus());
            o.setDictType(dictType);
            o.setValue(value);
        }));

        // 调用，校验异常
        assertServiceException(() -> dictDataService.validateDictDataValueUnique(id, dictType, value),
                DICT_DATA_VALUE_DUPLICATE);
    }
    
}
