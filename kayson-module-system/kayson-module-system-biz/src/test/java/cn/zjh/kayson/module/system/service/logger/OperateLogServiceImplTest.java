package cn.zjh.kayson.module.system.service.logger;

import cn.hutool.core.map.MapUtil;
import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.enums.UserTypeEnum;
import cn.zjh.kayson.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.operatelog.core.enums.OperateTypeEnum;
import cn.zjh.kayson.framework.test.core.ut.BaseDbUnitTest;
import cn.zjh.kayson.framework.test.core.util.RandomUtils;
import cn.zjh.kayson.module.system.api.logger.dto.OperateLogCreateReqDTO;
import cn.zjh.kayson.module.system.controller.admin.logger.vo.operatelog.OperateLogExportReqVO;
import cn.zjh.kayson.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.logger.OperateLogDO;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import cn.zjh.kayson.module.system.dal.mysql.logger.OperateLogMapper;
import cn.zjh.kayson.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.zjh.kayson.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.zjh.kayson.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.zjh.kayson.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.zjh.kayson.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.randomLong;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author zjh - kayson
 */
@Import(OperateLogServiceImpl.class)
public class OperateLogServiceImplTest extends BaseDbUnitTest {
    
    @Resource
    private OperateLogServiceImpl operateLogService;
    
    @Resource
    private OperateLogMapper operateLogMapper;
    
    @MockBean
    private AdminUserService adminUserService;

    @Test
    void testCreateOperateLog() {
        // mock 数据
        OperateLogCreateReqDTO reqDTO = randomPojo(OperateLogCreateReqDTO.class,
                o -> o.setExts(MapUtil.<String, Object>builder("orderId", randomLong()).build()));
        
        // 调用
        operateLogService.createOperateLog(reqDTO);
        // 断言
        OperateLogDO logDO = operateLogMapper.selectOne(null);
        assertPojoEquals(reqDTO, logDO);
    }

    @Test
    public void testGetOperateLogPage() {
        // mock（用户信息）
        AdminUserDO user = RandomUtils.randomPojo(AdminUserDO.class, o -> {
            o.setNickname("kayson");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(adminUserService.getUserListByNickname("kayson")).thenReturn(Collections.singletonList(user));
        Long userId = user.getId();

        // 构造操作日志
        OperateLogDO operateLogDO = RandomUtils.randomPojo(OperateLogDO.class, o -> {
            o.setUserId(userId);
            o.setUserType(randomEle(UserTypeEnum.values()).getValue());
            o.setModule("order");
            o.setType(OperateTypeEnum.CREATE.getType());
            o.setStartTime(buildTime(2023, 5, 30));
            o.setResultCode(GlobalErrorCodeConstants.SUCCESS.getCode());
            o.setExts(MapUtil.<String, Object>builder("orderId", randomLong()).build());
        });
        operateLogMapper.insert(operateLogDO);
        // 测试 userId 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setUserId(userId + 1)));
        // 测试 module 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setModule("user")));
        // 测试 type 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setType(OperateTypeEnum.IMPORT.getType())));
        // 测试 createTime 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setStartTime(buildTime(2023, 5, 1))));
        // 测试 resultCode 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setResultCode(BAD_REQUEST.getCode())));

        // 构造调用参数
        OperateLogPageReqVO reqVO = new OperateLogPageReqVO();
        reqVO.setUserNickname("kayson");
        reqVO.setModule("order");
        reqVO.setType(OperateTypeEnum.CREATE.getType());
        reqVO.setStartTime(buildBetweenTime(2023, 5, 29, 2023, 5, 31));
        reqVO.setSuccess(true);

        // 调用
        PageResult<OperateLogDO> pageResult = operateLogService.getOperateLogPage(reqVO);
        // 断言，只查到了一条符合条件的
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(operateLogDO, pageResult.getList().get(0));
    }

    @Test
    public void testGetOperateLogs() {
        // mock（用户信息）
        AdminUserDO user = RandomUtils.randomPojo(AdminUserDO.class, o -> {
            o.setNickname("kayson");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(adminUserService.getUserListByNickname("kayson")).thenReturn(Collections.singletonList(user));
        Long userId = user.getId();

        // 构造操作日志
        OperateLogDO operateLogDO = RandomUtils.randomPojo(OperateLogDO.class, o -> {
            o.setUserId(userId);
            o.setUserType(randomEle(UserTypeEnum.values()).getValue());
            o.setModule("order");
            o.setType(OperateTypeEnum.CREATE.getType());
            o.setStartTime(buildTime(2023, 5, 30));
            o.setResultCode(GlobalErrorCodeConstants.SUCCESS.getCode());
            o.setExts(MapUtil.<String, Object>builder("orderId", randomLong()).build());
        });
        operateLogMapper.insert(operateLogDO);
        // 测试 userId 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setUserId(userId + 1)));
        // 测试 module 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setModule("user")));
        // 测试 type 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setType(OperateTypeEnum.IMPORT.getType())));
        // 测试 createTime 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setStartTime(buildTime(2023, 5, 6))));
        // 测试 resultCode 不匹配
        operateLogMapper.insert(cloneIgnoreId(operateLogDO, o -> o.setResultCode(BAD_REQUEST.getCode())));

        // 构造调用参数
        OperateLogExportReqVO reqVO = new OperateLogExportReqVO();
        reqVO.setUserNickname("kayson");
        reqVO.setModule("order");
        reqVO.setType(OperateTypeEnum.CREATE.getType());
        reqVO.setStartTime(buildBetweenTime(2023, 5, 29, 2023, 5, 31));
        reqVO.setSuccess(true);

        // 调用 service 方法
        List<OperateLogDO> list = operateLogService.getOperateLogList(reqVO);
        // 断言，只查到了一条符合条件的
        assertEquals(1, list.size());
        assertPojoEquals(operateLogDO, list.get(0));
    }
    
}
