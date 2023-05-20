package cn.zjh.kayson.module.system.service.oauth2;

import cn.hutool.core.util.RandomUtil;
import cn.zjh.kayson.framework.common.enums.UserTypeEnum;
import cn.zjh.kayson.framework.common.exception.ErrorCode;
import cn.zjh.kayson.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.zjh.kayson.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2RefreshTokenDO;
import cn.zjh.kayson.module.system.dal.mysql.oauth2.OAuth2AccessTokenMapper;
import cn.zjh.kayson.module.system.dal.mysql.oauth2.OAuth2RefreshTokenMapper;
import cn.zjh.kayson.module.system.dal.redis.oauth2.OAuth2AccessTokenRedisDAO;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author zjh - kayson
 */
@Import({OAuth2TokenServiceImpl.class, OAuth2AccessTokenRedisDAO.class})
public class OAuth2TokenServiceImplTest extends BaseDbAndRedisUnitTest {

    @Resource
    private OAuth2TokenServiceImpl oauth2TokenService;
    
    @Resource
    private OAuth2AccessTokenMapper oauth2AccessTokenMapper;
    
    @Resource
    private OAuth2RefreshTokenMapper oauth2RefreshTokenMapper;
    
    @Resource
    private OAuth2AccessTokenRedisDAO oauth2AccessTokenRedisDAO;
    
    @MockBean
    private OAuth2ClientService oauth2ClientService;

    @Test
    void testCreateAccessToken() {
        // 准备参数
        Long userId = randomLong();
        Integer userType = RandomUtil.randomEle(UserTypeEnum.values()).getValue();
        String clientId = randomString();
        List<String> scopes = Lists.newArrayList("read", "write");

        // mock 方法
        OAuth2ClientDO clientDO = randomPojo(OAuth2ClientDO.class).setClientId(clientId)
                .setAccessTokenValiditySeconds(30).setRefreshTokenValiditySeconds(60);
        when(oauth2ClientService.validOAuthClient(eq(clientId))).thenReturn(clientDO);

        // 调用
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.createAccessToken(userId, userType, clientId, scopes);
        // 断言访问令牌
        OAuth2AccessTokenDO dbAccessTokenDO = oauth2AccessTokenMapper.selectByAccessToken(accessTokenDO.getAccessToken());
        assertPojoEquals(accessTokenDO, dbAccessTokenDO, "createTime", "updateTime", "deleted");
        assertEquals(userId, accessTokenDO.getUserId());
        assertEquals(userType, accessTokenDO.getUserType());
        assertEquals(clientId, accessTokenDO.getClientId());
        assertEquals(scopes, accessTokenDO.getScopes());
        assertFalse(LocalDateTime.now().isAfter(accessTokenDO.getExpiresTime()));
        // 断言访问令牌的缓存
        OAuth2AccessTokenDO redisAccessTokenDO = oauth2AccessTokenRedisDAO.get(accessTokenDO.getAccessToken());
        assertPojoEquals(accessTokenDO, redisAccessTokenDO, "createTime", "updateTime", "deleted");
        // 断言刷新令牌
        OAuth2RefreshTokenDO refreshTokenDO = oauth2RefreshTokenMapper.selectList().get(0);
        assertPojoEquals(accessTokenDO, refreshTokenDO, "id", "expiresTime", "createTime", "updateTime", "deleted");
        assertFalse(LocalDateTime.now().isAfter(refreshTokenDO.getExpiresTime()));
    }

    @Test
    void testRefreshAccessToken_null() {
        // 准备参数
        String refreshToken = randomString();
        String clientId = randomString();

        // 调用，并断言异常
        assertServiceException(() -> oauth2TokenService.refreshAccessToken(refreshToken, clientId), 
                new ErrorCode(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "无效的刷新令牌"));
    }

    @Test
    void testRefreshAccessToken_clientIdError() {
        // 准备参数
        String refreshToken = randomString();
        String clientId = randomString();
        // mock 方法
        OAuth2ClientDO clientDO = randomPojo(OAuth2ClientDO.class).setClientId(clientId);
        when(oauth2ClientService.validOAuthClient(eq(clientId))).thenReturn(clientDO);
        // mock 数据（刷新令牌）
        OAuth2RefreshTokenDO refreshTokenDO = randomPojo(OAuth2RefreshTokenDO.class)
                .setRefreshToken(refreshToken).setClientId("error");
        oauth2RefreshTokenMapper.insert(refreshTokenDO);

        // 调用，并断言异常
        assertServiceException(() -> oauth2TokenService.refreshAccessToken(refreshToken, clientId),
                new ErrorCode(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "刷新令牌的客户端编号不正确"));
    }

    @Test
    void testRefreshAccessToken_expired() {
        // 准备参数
        String refreshToken = randomString();
        String clientId = randomString();
        // mock 方法
        OAuth2ClientDO clientDO = randomPojo(OAuth2ClientDO.class).setClientId(clientId);
        when(oauth2ClientService.validOAuthClient(eq(clientId))).thenReturn(clientDO);
        // mock 数据（刷新令牌）
        OAuth2RefreshTokenDO refreshTokenDO = randomPojo(OAuth2RefreshTokenDO.class)
                .setRefreshToken(refreshToken).setClientId(clientId)
                .setExpiresTime(LocalDateTime.now().minusDays(1));
        oauth2RefreshTokenMapper.insert(refreshTokenDO);

        // 调用，并断言异常
        assertServiceException(() -> oauth2TokenService.refreshAccessToken(refreshToken, clientId),
                new ErrorCode(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "刷新令牌已过期"));
        assertEquals(0, oauth2RefreshTokenMapper.selectCount());
    }

    @Test
    void testRefreshAccessToken_success() {
        // 准备参数
        String refreshToken = randomString();
        String clientId = randomString();
        // mock 方法
        OAuth2ClientDO clientDO = randomPojo(OAuth2ClientDO.class).setClientId(clientId)
                .setAccessTokenValiditySeconds(30);
        when(oauth2ClientService.validOAuthClient(eq(clientId))).thenReturn(clientDO);
        // mock 数据（刷新令牌）
        OAuth2RefreshTokenDO refreshTokenDO = randomPojo(OAuth2RefreshTokenDO.class)
                .setRefreshToken(refreshToken).setClientId(clientId)
                .setExpiresTime(LocalDateTime.now().plusDays(1));
        oauth2RefreshTokenMapper.insert(refreshTokenDO);
        // mock 旧数据（访问令牌）
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class).setRefreshToken(refreshToken);
        oauth2AccessTokenMapper.insert(accessTokenDO);
        oauth2AccessTokenRedisDAO.set(accessTokenDO);

        // 调用
        OAuth2AccessTokenDO newAccessTokenDO = oauth2TokenService.refreshAccessToken(refreshToken, clientId);
        // 断言，老的访问令牌被删除
        assertNull(oauth2AccessTokenMapper.selectByAccessToken(accessTokenDO.getAccessToken()));
        assertNull(oauth2AccessTokenRedisDAO.get(accessTokenDO.getAccessToken()));
        // 断言，新的访问令牌
        OAuth2AccessTokenDO dbAccessTokenDO = oauth2AccessTokenMapper.selectByAccessToken(newAccessTokenDO.getAccessToken());
        assertPojoEquals(newAccessTokenDO, dbAccessTokenDO, "createTime", "updateTime", "deleted");
        assertPojoEquals(newAccessTokenDO, refreshTokenDO, "id", "expiresTime", "createTime", "updateTime", 
                "deleted", "creator", "updater");
        assertFalse(LocalDateTime.now().isAfter(newAccessTokenDO.getExpiresTime()));
        // 断言，新的访问令牌的缓存
        OAuth2AccessTokenDO redisAccessTokenDO = oauth2AccessTokenRedisDAO.get(newAccessTokenDO.getAccessToken());
        assertPojoEquals(newAccessTokenDO, redisAccessTokenDO);
    }

    @Test
    void testGetAccessToken() {
        // mock 数据（访问令牌）
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class)
                .setExpiresTime(LocalDateTime.now().plusDays(1));
        oauth2AccessTokenMapper.insert(accessTokenDO);
        // 准备参数
        String accessToken = accessTokenDO.getAccessToken();

        // 调用
        OAuth2AccessTokenDO result = oauth2TokenService.getAccessToken(accessToken);
        // 断言
        assertPojoEquals(accessTokenDO, result, "createTime", "updateTime", "deleted", "creator", "updater");

        // 调用，断言缓存
        OAuth2AccessTokenDO redisAccessTokenDO = oauth2AccessTokenRedisDAO.get(accessToken);
        assertPojoEquals(accessTokenDO, redisAccessTokenDO, "createTime", "updateTime", 
                "deleted", "creator", "updater");
    }

    @Test
    void testCheckAccessToken_null() {
        // 调用，并断言异常
        assertServiceException(() -> oauth2TokenService.checkAccessToken(randomString()), 
                new ErrorCode(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "访问令牌不存在"));
    }

    @Test
    void testCheckAccessToken_expired() {
        // mock 数据（访问令牌）
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class)
                .setExpiresTime(LocalDateTime.now().minusDays(1));
        oauth2AccessTokenMapper.insert(accessTokenDO);
        // 准备参数
        String accessToken = accessTokenDO.getAccessToken();

        // 调用，并断言异常
        assertServiceException(() -> oauth2TokenService.checkAccessToken(accessToken), 
                new ErrorCode(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "访问令牌已过期"));
    }

    @Test
    void testCheckAccessToken_success() {
        // mock 数据（访问令牌）
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class)
                .setExpiresTime(LocalDateTime.now().plusDays(1));
        oauth2AccessTokenMapper.insert(accessTokenDO);
        // 准备参数
        String accessToken = accessTokenDO.getAccessToken();

        // 调用，并断言
        OAuth2AccessTokenDO dbAccessTokenDO = oauth2TokenService.checkAccessToken(accessToken);
        assertPojoEquals(accessTokenDO, dbAccessTokenDO,"createTime", "updateTime", 
                "deleted", "creator", "updater");
    }

    @Test
    void testRemoveAccessToken_null() {
        // 调用，并断言
        assertNull(oauth2TokenService.removeAccessToken(randomString()));
    }

    @Test
    void testRemoveAccessToken_success() {
        // mock 数据（访问令牌）
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class)
                .setExpiresTime(LocalDateTime.now().plusDays(1));
        oauth2AccessTokenMapper.insert(accessTokenDO);
        // mock 数据（刷新令牌）
        OAuth2RefreshTokenDO refreshTokenDO = randomPojo(OAuth2RefreshTokenDO.class)
                .setRefreshToken(accessTokenDO.getRefreshToken());
        oauth2RefreshTokenMapper.insert(refreshTokenDO);

        // 调用，并断言
        OAuth2AccessTokenDO dbAccessTokenDO = oauth2TokenService.removeAccessToken(accessTokenDO.getAccessToken());
        assertPojoEquals(accessTokenDO, dbAccessTokenDO, "createTime", "updateTime", "deleted", 
                "creator", "updater");
        // 断言数据
        assertNull(oauth2AccessTokenMapper.selectByAccessToken(accessTokenDO.getAccessToken()));
        assertNull(oauth2RefreshTokenMapper.selectByRefreshToken(accessTokenDO.getRefreshToken()));
        assertNull(oauth2AccessTokenRedisDAO.get(accessTokenDO.getAccessToken()));
    }
    
}
