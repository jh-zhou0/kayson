package cn.zjh.kayson.framework.operatelog.core.service;

import cn.hutool.core.bean.BeanUtil;
import cn.zjh.kayson.module.system.api.logger.OperateLogApi;
import cn.zjh.kayson.module.system.api.logger.dto.OperateLogCreateReqDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;

/**
 * 操作日志 Framework Service 实现类
 * 
 * 基于 {@link OperateLogApi} 实现，记录操作日志
 * 
 * @author zjh - kayson
 */
@RequiredArgsConstructor
public class OperateLogFrameworkServiceImpl implements OperateLogFrameworkService {
    
    private final OperateLogApi operateLogApi;
    
    @Override
    @Async
    public void createOperateLog(OperateLog operateLog) {
        OperateLogCreateReqDTO createReqDTO = BeanUtil.copyProperties(operateLog, OperateLogCreateReqDTO.class);
        operateLogApi.createOperateLog(createReqDTO);    
    }
    
}
