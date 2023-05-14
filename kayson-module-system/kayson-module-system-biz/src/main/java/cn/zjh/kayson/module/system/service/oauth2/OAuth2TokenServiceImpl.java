package cn.zjh.kayson.module.system.service.oauth2;

import cn.hutool.core.util.IdUtil;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2RefreshTokenDO;
import cn.zjh.kayson.module.system.dal.mysql.oauth2.OAuth2AccessTokenMapper;
import cn.zjh.kayson.module.system.dal.mysql.oauth2.OAuth2RefreshTokenMapper;
import cn.zjh.kayson.module.system.dal.redis.oauth2.OAuth2AccessTokenRedisDAO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OAuth2.0 Token Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
public class OAuth2TokenServiceImpl implements OAuth2TokenService {
    
    @Resource
    private OAuth2AccessTokenMapper oAuth2AccessTokenMapper;
    @Resource
    private OAuth2RefreshTokenMapper oAuth2RefreshTokenMapper;
    
    @Resource
    private OAuth2ClientService oAuth2ClientService;
    
    @Resource
    private OAuth2AccessTokenRedisDAO oAuth2AccessTokenRedisDAO;
    
    @Override
    public OAuth2AccessTokenDO createAccessToken(Long userId, Integer userType, String clientId, List<String> scopes) {
        OAuth2ClientDO client = oAuth2ClientService.validOAuthClient(clientId);
        // 创建刷新令牌
        OAuth2RefreshTokenDO refreshTokenDO = createOAuth2RefreshToken(userId, userType, client, scopes);
        // 创建访问令牌
        return createOAuth2AccessToken(refreshTokenDO, client);
    }

    private OAuth2AccessTokenDO createOAuth2AccessToken(OAuth2RefreshTokenDO refreshTokenDO, OAuth2ClientDO client) {
        OAuth2AccessTokenDO accessTokenDO = new OAuth2AccessTokenDO()
                .setAccessToken(generateToken())
                .setUserId(refreshTokenDO.getUserId())
                .setUserType(refreshTokenDO.getUserType())
                .setClientId(client.getClientId())
                .setScopes(refreshTokenDO.getScopes())
                .setRefreshToken(refreshTokenDO.getRefreshToken())
                .setExpiresTime(LocalDateTime.now().plusSeconds(client.getAccessTokenValiditySeconds()));
        oAuth2AccessTokenMapper.insert(accessTokenDO);
        // 记录到 Redis 中
        oAuth2AccessTokenRedisDAO.set(accessTokenDO);
        return accessTokenDO;
    }

    private OAuth2RefreshTokenDO createOAuth2RefreshToken(Long userId, Integer userType, 
                                                          OAuth2ClientDO client, List<String> scopes) {
        OAuth2RefreshTokenDO refreshTokenDO = new OAuth2RefreshTokenDO()
                .setRefreshToken(generateToken())
                .setUserId(userId).setUserType(userType)
                .setClientId(client.getClientId()).setScopes(scopes)
                .setExpiresTime(LocalDateTime.now().plusSeconds(client.getRefreshTokenValiditySeconds()));
        oAuth2RefreshTokenMapper.insert(refreshTokenDO);
        return refreshTokenDO;
    }
    
    private static String generateToken() {
        return IdUtil.fastSimpleUUID();
    }

}
