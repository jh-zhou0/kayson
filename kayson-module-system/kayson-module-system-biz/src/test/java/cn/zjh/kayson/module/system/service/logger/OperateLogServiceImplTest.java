package cn.zjh.kayson.module.system.service.logger;

import cn.hutool.core.map.MapUtil;
import cn.zjh.kayson.framework.test.core.ut.BaseDbUnitTest;
import cn.zjh.kayson.module.system.api.logger.dto.OperateLogCreateReqDTO;
import cn.zjh.kayson.module.system.dal.dataobject.logger.OperateLogDO;
import cn.zjh.kayson.module.system.dal.mysql.logger.OperateLogMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.randomLong;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.randomPojo;

/**
 * @author zjh - kayson
 */
@Import(OperateLogServiceImpl.class)
public class OperateLogServiceImplTest extends BaseDbUnitTest {
    
    @Resource
    private OperateLogServiceImpl operateLogService;
    
    @Resource
    private OperateLogMapper operateLogMapper;

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
    
}
