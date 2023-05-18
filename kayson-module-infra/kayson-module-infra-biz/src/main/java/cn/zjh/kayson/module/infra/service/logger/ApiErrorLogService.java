package cn.zjh.kayson.module.infra.service.logger;

import cn.zjh.kayson.module.infra.api.logger.dto.ApiErrorLogCreateReqDTO;

/**
 * API 错误日志 Service 接口
 * 
 * @author zjh - kayson
 */
public interface ApiErrorLogService {

    /**
     * 创建 API 错误日志
     *
     * @param createReqDTO API 错误日志
     */
    void createApiErrorLog(ApiErrorLogCreateReqDTO createReqDTO);
    
}
