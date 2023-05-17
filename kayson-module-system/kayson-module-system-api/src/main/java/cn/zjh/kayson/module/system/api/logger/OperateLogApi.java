package cn.zjh.kayson.module.system.api.logger;

import cn.zjh.kayson.module.system.api.logger.dto.OperateLogCreateReqDTO;

/**
 * 操作日志 API 接口
 * 
 * @author zjh - kayson
 */
public interface OperateLogApi {

    /**
     * 创建操作日志
     *
     * @param createReqDTO 请求
     */
    void createOperateLog(OperateLogCreateReqDTO createReqDTO);
    
}
