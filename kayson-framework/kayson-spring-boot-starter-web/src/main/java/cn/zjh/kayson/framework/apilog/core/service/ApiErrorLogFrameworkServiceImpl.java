package cn.zjh.kayson.framework.apilog.core.service;

import cn.hutool.core.bean.BeanUtil;
import cn.zjh.kayson.module.infra.api.logger.ApiErrorLogApi;
import cn.zjh.kayson.module.infra.api.logger.dto.ApiErrorLogCreateReqDTO;
import lombok.RequiredArgsConstructor;

/**
 * API 错误日志 Framework Service 实现类
 * 
 * @author zjh - kayson
 */
@RequiredArgsConstructor
public class ApiErrorLogFrameworkServiceImpl implements ApiErrorLogFrameworkService {
    
    private final ApiErrorLogApi apiErrorLogApi;
    
    @Override
    public void createApiErrorLog(ApiErrorLog apiErrorLog) {
        ApiErrorLogCreateReqDTO reqDTO = BeanUtil.copyProperties(apiErrorLog, ApiErrorLogCreateReqDTO.class);
        apiErrorLogApi.createApiErrorLog(reqDTO);
    }
    
}
