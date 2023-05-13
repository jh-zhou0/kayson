package cn.zjh.kayson.module.system.service.oauth2;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.oauth2.vo.OAuth2ClientCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.oauth2.vo.OAuth2ClientPageReqVO;
import cn.zjh.kayson.module.system.controller.admin.oauth2.vo.OAuth2ClientUpdateReqVO;
import cn.zjh.kayson.module.system.convert.oauth2.OAuth2ClientConvert;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import cn.zjh.kayson.module.system.dal.mysql.oauth2.OAuth2ClientMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static cn.zjh.kayson.framework.common.exception.util.ServiceExceptionUtils.exception;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;

/**
 * OAuth2.0 Client Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
public class OAuth2ClientServiceImpl implements OAuth2ClientService {
    
    @Resource
    private OAuth2ClientMapper oAuth2ClientMapper;
    
    @Override
    public Long createOAuth2Client(OAuth2ClientCreateReqVO createReqVO) {
        // 校验 Client 未被占用
        validateClientIdExists(null, createReqVO.getClientId());
        // 插入客户端
        OAuth2ClientDO client = OAuth2ClientConvert.INSTANCE.convert(createReqVO);
        oAuth2ClientMapper.insert(client);
        return client.getId();
    }

    @Override
    public void updateOAuth2Client(OAuth2ClientUpdateReqVO updateReqVO) {
        // 校验存在
        validateOAuth2ClientExists(updateReqVO.getId());
        // 校验 Client 未被占用
        validateClientIdExists(updateReqVO.getId(), updateReqVO.getClientId());
        // 更新客户端
        OAuth2ClientDO updateObj = OAuth2ClientConvert.INSTANCE.convert(updateReqVO);
        oAuth2ClientMapper.updateById(updateObj);
    }

    @Override
    public void deleteOAuth2Client(Long id) {
        // 校验存在
        validateOAuth2ClientExists(id);
        // 删除客户端
        oAuth2ClientMapper.deleteById(id);
    }

    @Override
    public OAuth2ClientDO getOAuth2Client(Long id) {
        return oAuth2ClientMapper.selectById(id);
    }

    @Override
    public PageResult<OAuth2ClientDO> getOAuth2ClientPage(OAuth2ClientPageReqVO pageReqVO) {
        return oAuth2ClientMapper.selectPage(pageReqVO);
    }

    private void validateOAuth2ClientExists(Long id) {
        if (oAuth2ClientMapper.selectById(id) == null) {
            throw exception(OAUTH2_CLIENT_NOT_EXISTS);
        }
    }

    private void validateClientIdExists(Long id, String clientId) {
        OAuth2ClientDO client = oAuth2ClientMapper.selectByClientId(clientId);
        if (client == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的客户端
        if (id == null) {
            throw exception(OAUTH2_CLIENT_EXISTS);
        }
        if (!id.equals(client.getId())) {
            throw exception(OAUTH2_CLIENT_EXISTS);
        }
    }
}
