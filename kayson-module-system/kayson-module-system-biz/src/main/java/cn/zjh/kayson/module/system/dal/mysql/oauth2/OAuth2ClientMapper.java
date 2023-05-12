package cn.zjh.kayson.module.system.dal.mysql.oauth2;

import cn.zjh.kayson.framework.mybatis.core.mapper.BaseMapperX;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zjh - kayson
 */
@Mapper
public interface OAuth2ClientMapper extends BaseMapperX<OAuth2ClientDO> {
}
