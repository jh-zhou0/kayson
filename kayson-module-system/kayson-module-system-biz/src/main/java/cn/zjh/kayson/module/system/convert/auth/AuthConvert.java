package cn.zjh.kayson.module.system.convert.auth;

import cn.zjh.kayson.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author zjh - kayson
 */
@Mapper
public interface AuthConvert {

    AuthConvert INSTANCE = Mappers.getMapper(AuthConvert.class);

    AuthLoginRespVO convert(OAuth2AccessTokenDO bean);
    
}
