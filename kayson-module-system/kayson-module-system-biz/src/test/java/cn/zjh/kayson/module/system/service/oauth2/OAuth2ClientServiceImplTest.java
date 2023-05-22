package cn.zjh.kayson.module.system.service.oauth2;

import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.test.core.ut.BaseDbUnitTest;
import cn.zjh.kayson.module.system.controller.admin.oauth2.vo.OAuth2ClientCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.oauth2.vo.OAuth2ClientPageReqVO;
import cn.zjh.kayson.module.system.controller.admin.oauth2.vo.OAuth2ClientUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import cn.zjh.kayson.module.system.dal.mysql.oauth2.OAuth2ClientMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Collections;

import static cn.zjh.kayson.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.*;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author zjh - kayson
 */
@Import(OAuth2ClientServiceImpl.class)
public class OAuth2ClientServiceImplTest extends BaseDbUnitTest {
    
    @Resource
    private OAuth2ClientServiceImpl oauth2ClientService;

    @Resource
    private OAuth2ClientMapper oauth2ClientMapper;

    @Test
    void testCreateOAuth2Client_success() {
        // 准备参数
        OAuth2ClientCreateReqVO reqVO = randomPojo(OAuth2ClientCreateReqVO.class, o -> o.setLogo(randomString()));

        // 调用
        Long clientId = oauth2ClientService.createOAuth2Client(reqVO);
        // 断言
        assertNotNull(clientId);
        // 校验记录的属性是否正确
        OAuth2ClientDO clientDO = oauth2ClientMapper.selectById(clientId);
        assertPojoEquals(reqVO, clientDO);
    }

    @Test
    void testUpdateOAuth2Client_success() {
        // mock 数据
        OAuth2ClientDO dbOAuth2Client = randomPojo(OAuth2ClientDO.class);
        oauth2ClientMapper.insert(dbOAuth2Client);
        // 准备参数
        OAuth2ClientUpdateReqVO reqVO = randomPojo(OAuth2ClientUpdateReqVO.class, o ->
                o.setId(dbOAuth2Client.getId()).setLogo(randomString()));
        
        // 调用
        oauth2ClientService.updateOAuth2Client(reqVO);
        // 校验是否更新正确
        OAuth2ClientDO clientDO = oauth2ClientMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, clientDO);
    }

    @Test
    void testUpdateOAuth2Client_notExists() {
        // 准备参数
        OAuth2ClientUpdateReqVO reqVO = randomPojo(OAuth2ClientUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> oauth2ClientService.updateOAuth2Client(reqVO), OAUTH2_CLIENT_NOT_EXISTS);
    }

    @Test
    void testDeleteOAuth2Client_success() {
        // mock 数据
        OAuth2ClientDO dbOAuth2Client = randomPojo(OAuth2ClientDO.class);
        oauth2ClientMapper.insert(dbOAuth2Client);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbOAuth2Client.getId();

        // 调用
        oauth2ClientService.deleteOAuth2Client(id);
        // 校验数据不存在了
        assertNull(oauth2ClientMapper.selectById(id));
    }

    @Test
    void testDeleteOAuth2Client_notExists() {
        // 准备参数
        Long id = randomLong();

        // 调用, 并断言异常
        assertServiceException(() -> oauth2ClientService.deleteOAuth2Client(id), OAUTH2_CLIENT_NOT_EXISTS);
    }

    @Test
    void testValidateClientIdExists_noId() {
        // mock 数据
        OAuth2ClientDO client = randomPojo(OAuth2ClientDO.class).setClientId("chip");
        oauth2ClientMapper.insert(client);
        // 准备参数
        String clientId = "chip";

        // 调用, 并断言异常
        assertServiceException(() -> oauth2ClientService.validateClientIdExists(null, clientId), OAUTH2_CLIENT_EXISTS);
    }

    @Test
    void testValidateClientIdExists_withId() {
        // mock 数据
        OAuth2ClientDO client = randomPojo(OAuth2ClientDO.class).setClientId("chip");
        oauth2ClientMapper.insert(client);
        // 准备参数
        Long id = randomLong();
        String clientId = "chip";

        // 调用, 并断言异常
        assertServiceException(() -> oauth2ClientService.validateClientIdExists(id, clientId), OAUTH2_CLIENT_EXISTS);
    }

    @Test
    void testGetOAuth2Client() {
        // mock 数据
        OAuth2ClientDO clientDO = randomPojo(OAuth2ClientDO.class);
        oauth2ClientMapper.insert(clientDO);
        // 准备参数
        Long id = clientDO.getId();

        // 调用，并断言
        OAuth2ClientDO client = oauth2ClientService.getOAuth2Client(id);
        assertPojoEquals(clientDO, client);
    }

    @Test
    void testGetOAuth2ClientPage() {
        // mock 数据
        OAuth2ClientDO dbOAuth2Client = randomPojo(OAuth2ClientDO.class, o -> { // 等会查询到
            o.setName("潜龙");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        oauth2ClientMapper.insert(dbOAuth2Client);
        // 测试 name 不匹配
        oauth2ClientMapper.insert(cloneIgnoreId(dbOAuth2Client, o -> o.setName("凤凰")));
        // 测试 status 不匹配
        oauth2ClientMapper.insert(cloneIgnoreId(dbOAuth2Client, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 准备参数
        OAuth2ClientPageReqVO reqVO = new OAuth2ClientPageReqVO();
        reqVO.setName("龙");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        PageResult<OAuth2ClientDO> pageResult = oauth2ClientService.getOAuth2ClientPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbOAuth2Client, pageResult.getList().get(0));
    }

    @Test
    void testValidOAuthClient() {
        // mock 数据
        OAuth2ClientDO client = randomPojo(OAuth2ClientDO.class).setClientId("default")
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        OAuth2ClientDO client02 = randomPojo(OAuth2ClientDO.class).setClientId("disable")
                .setStatus(CommonStatusEnum.DISABLE.getStatus());
        oauth2ClientMapper.insert(client);
        oauth2ClientMapper.insert(client02);

        // 调用，并断言
        assertServiceException(() -> oauth2ClientService.validOAuthClient(randomString(), 
                null, null, null, null), OAUTH2_CLIENT_NOT_EXISTS);
        assertServiceException(() -> oauth2ClientService.validOAuthClient("disable",
                null, null, null, null), OAUTH2_CLIENT_DISABLE);
        assertServiceException(() -> oauth2ClientService.validOAuthClient("default",
                randomString(), null, null, null), OAUTH2_CLIENT_CLIENT_SECRET_ERROR);
        assertServiceException(() -> oauth2ClientService.validOAuthClient("default",
                null, randomString(), null, null), OAUTH2_CLIENT_AUTHORIZED_GRANT_TYPE_NOT_EXISTS);
        assertServiceException(() -> oauth2ClientService.validOAuthClient("default",
                null, null, Collections.singleton(randomString()), null), OAUTH2_CLIENT_SCOPE_OVER);
        assertServiceException(() -> oauth2ClientService.validOAuthClient("default",
                null, null, null, "test"), OAUTH2_CLIENT_REDIRECT_URI_NOT_MATCH, "test");
        // 成功调用（1：参数完整）
        OAuth2ClientDO result = oauth2ClientService.validOAuthClient(client.getClientId(), client.getSecret(),
                client.getAuthorizedGrantTypes().get(0), client.getScopes(), client.getRedirectUris().get(0));
        assertPojoEquals(client, result);
        // 成功调用（2：只有 clientId 参数）
        result = oauth2ClientService.validOAuthClient(client.getClientId());
        assertPojoEquals(client, result);
    }
}
