package cn.zjh.kayson.module.system.convert.oauth2;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.oauth2.vo.OAuth2ClientCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.oauth2.vo.OAuth2ClientRespVO;
import cn.zjh.kayson.module.system.controller.admin.oauth2.vo.OAuth2ClientUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author zjh - kayson
 */
@Mapper
public interface OAuth2ClientConvert {

    OAuth2ClientConvert INSTANCE = Mappers.getMapper(OAuth2ClientConvert.class);
    
    OAuth2ClientDO convert(OAuth2ClientCreateReqVO bean);
    
    OAuth2ClientDO convert(OAuth2ClientUpdateReqVO bean);

    OAuth2ClientRespVO convert(OAuth2ClientDO bean);

    PageResult<OAuth2ClientRespVO> convertPage(PageResult<OAuth2ClientDO> page);
}
