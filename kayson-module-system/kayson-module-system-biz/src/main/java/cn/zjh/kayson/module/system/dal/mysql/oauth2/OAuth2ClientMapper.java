package cn.zjh.kayson.module.system.dal.mysql.oauth2;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.mybatis.core.mapper.BaseMapperX;
import cn.zjh.kayson.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.zjh.kayson.module.system.controller.admin.oauth2.vo.OAuth2ClientPageReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zjh - kayson
 */
@Mapper
public interface OAuth2ClientMapper extends BaseMapperX<OAuth2ClientDO> {
    
    default OAuth2ClientDO selectByClientId(String clientId) {
        return selectOne(OAuth2ClientDO::getClientId, clientId);
    }

    default PageResult<OAuth2ClientDO> selectPage(OAuth2ClientPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<OAuth2ClientDO>()
                .likeIfPresent(OAuth2ClientDO::getName, pageReqVO.getName())
                .eqIfPresent(OAuth2ClientDO::getStatus, pageReqVO.getStatus())
                .orderByDesc(OAuth2ClientDO::getId));
    }
}
