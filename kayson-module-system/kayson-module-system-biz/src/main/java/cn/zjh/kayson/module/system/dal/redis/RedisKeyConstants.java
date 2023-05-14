package cn.zjh.kayson.module.system.dal.redis;

import cn.zjh.kayson.framework.redis.core.RedisKeyDefine;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;

/**
 * System Redis Key 枚举类
 * 
 * @author zjh - kayson
 */
public interface RedisKeyConstants {
    
    RedisKeyDefine OAUTH2_ACCESS_TOKEN = new RedisKeyDefine("访问令牌的缓存",
            "oauth2_access_token:%s", // 参数为访问令牌 token
            RedisKeyDefine.KeyTypeEnum.STRING,
            OAuth2AccessTokenDO.class, 
            RedisKeyDefine.TimeoutTypeEnum.DYNAMIC);
    
}
