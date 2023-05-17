package cn.zjh.kayson.module.system.service.logger;

import cn.zjh.kayson.module.system.api.logger.dto.OperateLogCreateReqDTO;

/**
 * 操作日志 Service 接口
 * 
 * @author zjh - kayson
 */
public interface OperateLogService {

    /**
     * 记录操作日志
     *
     * @param createReqDTO 操作日志请求
     */
    void createOperateLog(OperateLogCreateReqDTO createReqDTO);
    
}
