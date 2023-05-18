package cn.zjh.kayson.module.infra.api.logger;

import cn.zjh.kayson.module.infra.api.logger.dto.ApiErrorLogCreateReqDTO;
import cn.zjh.kayson.module.infra.service.logger.ApiErrorLogService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * API 访问日志的 API 接口
 * 
 * @author zjh - kayson
 */
@Service
@Validated
public class ApiErrorLogApiImpl implements ApiErrorLogApi {
    
    @Resource
    private ApiErrorLogService apiErrorLogService;
    
    @Override
    public void createApiErrorLog(ApiErrorLogCreateReqDTO createDTO) {
        apiErrorLogService.createApiErrorLog(createDTO);
    }
    
}
