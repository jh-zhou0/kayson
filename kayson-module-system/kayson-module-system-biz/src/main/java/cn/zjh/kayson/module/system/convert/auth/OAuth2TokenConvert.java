package cn.zjh.kayson.module.system.convert.auth;

import cn.zjh.kayson.module.system.api.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author zjh - kayson
 */
@Mapper
public interface OAuth2TokenConvert {

    OAuth2TokenConvert INSTANCE = Mappers.getMapper(OAuth2TokenConvert.class);
    
    OAuth2AccessTokenCheckRespDTO convert(OAuth2AccessTokenDO bean);
    
}
