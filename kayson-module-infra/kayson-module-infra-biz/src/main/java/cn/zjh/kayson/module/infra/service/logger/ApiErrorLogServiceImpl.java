package cn.zjh.kayson.module.infra.service.logger;

import cn.zjh.kayson.module.infra.api.logger.dto.ApiErrorLogCreateReqDTO;
import cn.zjh.kayson.module.infra.convert.logger.ApiErrorLogConvert;
import cn.zjh.kayson.module.infra.dal.dataobject.logger.ApiErrorLogDO;
import cn.zjh.kayson.module.infra.dal.mysql.logger.ApiErrorLogMapper;
import cn.zjh.kayson.module.infra.enums.logger.ApiErrorLogProcessStatusEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * API 错误日志 Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
public class ApiErrorLogServiceImpl implements ApiErrorLogService {

    @Resource
    private ApiErrorLogMapper apiErrorLogMapper;

    @Override
    public void createApiErrorLog(ApiErrorLogCreateReqDTO createReqDTO) {
        ApiErrorLogDO apiErrorLog = ApiErrorLogConvert.INSTANCE.convert(createReqDTO)
                .setProcessStatus(ApiErrorLogProcessStatusEnum.INIT.getStatus());
        apiErrorLogMapper.insert(apiErrorLog);
    }
    
}
