package cn.zjh.kayson.module.system.api.oauth2;

import cn.zjh.kayson.module.system.api.oauth2.dto.OAuth2AccessTokenCheckRespDTO;

/**
 * OAuth2.0 Token API 接口
 * 
 * @author zjh - kayson
 */
public interface OAuth2TokenApi {

    /**
     * 校验访问令牌
     *
     * @param accessToken 访问令牌
     * @return 访问令牌的信息
     */
    OAuth2AccessTokenCheckRespDTO checkAccessToken(String accessToken);
    
}
