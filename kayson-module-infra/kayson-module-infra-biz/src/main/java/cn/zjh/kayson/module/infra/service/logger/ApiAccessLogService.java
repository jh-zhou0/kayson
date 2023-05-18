package cn.zjh.kayson.module.infra.service.logger;

import cn.zjh.kayson.module.infra.api.logger.dto.ApiAccessLogCreateReqDTO;

/**
 * API 访问日志 Service 接口
 * 
 * @author zjh - kayson
 */
public interface ApiAccessLogService {

    /**
     * 创建 API 访问日志
     *
     * @param createReqDTO API 访问日志
     */
    void createApiAccessLog(ApiAccessLogCreateReqDTO createReqDTO);
    
}
