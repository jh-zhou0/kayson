package cn.zjh.kayson.module.system.service.logger;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.test.core.ut.BaseDbUnitTest;
import cn.zjh.kayson.module.system.api.logger.dto.LoginLogCreateReqDTO;
import cn.zjh.kayson.module.system.controller.admin.logger.vo.loginlog.LoginLogPageReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.logger.LoginLogDO;
import cn.zjh.kayson.module.system.dal.mysql.logger.LoginLogMapper;
import cn.zjh.kayson.module.system.enums.logger.LoginResultEnum;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.zjh.kayson.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.zjh.kayson.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.zjh.kayson.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author zjh - kayson
 */
@Import(LoginLogServiceImpl.class)
public class LoginLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private LoginLogServiceImpl loginLogService;

    @Resource
    private LoginLogMapper loginLogMapper;

    @Test
    void testGetLoginLogPage() {
        // mock 数据
        LoginLogDO loginLogDO = randomPojo(LoginLogDO.class, o -> o.setUserIp("192.168.200.3").setUsername("zjh")
                .setResult(LoginResultEnum.SUCCESS.getResult())
                .setCreateTime(buildTime(2023, 5, 20)));
        loginLogMapper.insert(loginLogDO);
        // 测试 status 不匹配
        loginLogMapper.insert(cloneIgnoreId(loginLogDO, o -> o.setResult(LoginResultEnum.CAPTCHA_CODE_ERROR.getResult())));
        // 测试 ip 不匹配
        loginLogMapper.insert(cloneIgnoreId(loginLogDO, o -> o.setUserIp("192.168.200.18")));
        // 测试 username 不匹配
        loginLogMapper.insert(cloneIgnoreId(loginLogDO, o -> o.setUsername("kayson")));
        // 测试 createTime 不匹配
        loginLogMapper.insert(cloneIgnoreId(loginLogDO, o ->
                o.setCreateTime(LocalDateTime.of(2023, 3, 8, 0, 0, 0))));
        // 构造调用参数
        LoginLogPageReqVO reqVO = new LoginLogPageReqVO()
                .setUserIp("192.168.200.3")
                .setUsername("zjh")
                .setStatus(true)
                .setCreateTime(buildBetweenTime(2023, 5, 19, 2023, 5, 21));

        // 调用
        PageResult<LoginLogDO> pageResult = loginLogService.getLoginLogPage(reqVO);
        // 断言，只查到了一条符合条件的
        assertEquals(1, pageResult.getList().size());
        assertEquals(1, pageResult.getTotal());
        assertPojoEquals(loginLogDO, pageResult.getList().get(0));
    }

    @Test
    void testCreateLoginLog() {
        LoginLogCreateReqDTO reqDTO = randomPojo(LoginLogCreateReqDTO.class);

        // 调用
        loginLogService.createLoginLog(reqDTO);
        // 断言
        LoginLogDO loginLogDO = loginLogMapper.selectOne(null);
        assertPojoEquals(reqDTO, loginLogDO);
    }
}
