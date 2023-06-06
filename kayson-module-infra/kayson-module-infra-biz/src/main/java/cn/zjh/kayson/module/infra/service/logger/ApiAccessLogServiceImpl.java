package cn.zjh.kayson.module.infra.service.logger;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.infra.api.logger.dto.ApiAccessLogCreateReqDTO;
import cn.zjh.kayson.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogExportReqVO;
import cn.zjh.kayson.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogPageReqVO;
import cn.zjh.kayson.module.infra.convert.logger.ApiAccessLogConvert;
import cn.zjh.kayson.module.infra.dal.dataobject.logger.ApiAccessLogDO;
import cn.zjh.kayson.module.infra.dal.mysql.logger.ApiAccessLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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

    @Override
    public PageResult<ApiAccessLogDO> getApiAccessLogPage(ApiAccessLogPageReqVO pageReqVO) {
        return apiAccessLogMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ApiAccessLogDO> getApiAccessLogList(ApiAccessLogExportReqVO exportReqVO) {
        return apiAccessLogMapper.selectList(exportReqVO);
    }
    
}
