package cn.zjh.kayson.module.system.service.logger;

import cn.hutool.core.util.StrUtil;
import cn.zjh.kayson.module.system.api.logger.dto.OperateLogCreateReqDTO;
import cn.zjh.kayson.module.system.convert.logger.OperateLogConvert;
import cn.zjh.kayson.module.system.dal.dataobject.logger.OperateLogDO;
import cn.zjh.kayson.module.system.dal.mysql.logger.OperateLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.zjh.kayson.module.system.dal.dataobject.logger.OperateLogDO.JAVA_METHOD_ARGS_MAX_LENGTH;
import static cn.zjh.kayson.module.system.dal.dataobject.logger.OperateLogDO.RESULT_MAX_LENGTH;

/**
 * 操作日志 Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
@Validated
public class OperateLogServiceImpl implements OperateLogService {
    
    @Resource
    private OperateLogMapper operateLogMapper;
    
    @Override
    public void createOperateLog(OperateLogCreateReqDTO createReqDTO) {
        OperateLogDO logDO = OperateLogConvert.INSTANCE.convert(createReqDTO);
        logDO.setJavaMethodArgs(StrUtil.maxLength(logDO.getJavaMethodArgs(), JAVA_METHOD_ARGS_MAX_LENGTH - 3));
        logDO.setResultData(StrUtil.maxLength(logDO.getResultData(), RESULT_MAX_LENGTH - 3));
        operateLogMapper.insert(logDO);
    }
}
