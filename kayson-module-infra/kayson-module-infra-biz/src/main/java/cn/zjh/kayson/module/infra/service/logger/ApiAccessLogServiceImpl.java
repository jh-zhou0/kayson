package cn.zjh.kayson.module.infra.service.logger;

import cn.zjh.kayson.module.infra.api.logger.dto.ApiAccessLogCreateReqDTO;
import cn.zjh.kayson.module.infra.convert.logger.ApiAccessLogConvert;
import cn.zjh.kayson.module.infra.dal.dataobject.logger.ApiAccessLogDO;
import cn.zjh.kayson.module.infra.dal.mysql.logger.ApiAccessLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * API 访问日志 Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
public class ApiAccessLogServiceImpl implements ApiAccessLogService {
    
    @Resource
    private ApiAccessLogMapper apiAccessLogMapper;
    
    @Override
    public void createApiAccessLog(ApiAccessLogCreateReqDTO createReqDTO) {
        ApiAccessLogDO apiAccessLog = ApiAccessLogConvert.INSTANCE.convert(createReqDTO);
        apiAccessLogMapper.insert(apiAccessLog);
    }
}
