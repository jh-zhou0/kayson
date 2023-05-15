package cn.zjh.kayson.module.system.api.oauth2;

import cn.zjh.kayson.module.system.api.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import cn.zjh.kayson.module.system.convert.auth.OAuth2TokenConvert;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.zjh.kayson.module.system.service.oauth2.OAuth2TokenService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * OAuth2.0 Token API 实现类
 * 
 * @author zjh - kayson
 */
@Service
public class OAuth2TokenApiImpl implements OAuth2TokenApi {
    
    @Resource
    private OAuth2TokenService oAuth2TokenService;
    
    @Override
    public OAuth2AccessTokenCheckRespDTO checkAccessToken(String accessToken) {
        OAuth2AccessTokenDO oAuth2AccessTokenDO = oAuth2TokenService.checkAccessToken(accessToken);
        return OAuth2TokenConvert.INSTANCE.convert(oAuth2AccessTokenDO);
    }
    
}
