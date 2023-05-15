package cn.zjh.kayson.module.system.dal.mysql.oauth2;

import cn.zjh.kayson.framework.mybatis.core.mapper.BaseMapperX;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface OAuth2AccessTokenMapper extends BaseMapperX<OAuth2AccessTokenDO> {

    default OAuth2AccessTokenDO selectByAccessToken(String accessToken) {
        return selectOne(OAuth2AccessTokenDO::getAccessToken, accessToken);    
    }

    default List<OAuth2AccessTokenDO> selectListByRefreshToken(String refreshToken) {
        return selectList(OAuth2AccessTokenDO::getRefreshToken, refreshToken);
    }
    
}
