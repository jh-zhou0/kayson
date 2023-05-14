package cn.zjh.kayson.module.system.service.logger;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.api.logger.dto.LoginLogCreateReqDTO;
import cn.zjh.kayson.module.system.controller.admin.logger.vo.loginlog.LoginLogPageReqVO;
import cn.zjh.kayson.module.system.convert.logger.LoginLogConvert;
import cn.zjh.kayson.module.system.dal.dataobject.logger.LoginLogDO;
import cn.zjh.kayson.module.system.dal.mysql.logger.LoginLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 登录日志 Service 实现
 * 
 * @author zjh - kayson
 */
@Service
public class LoginLogServiceImpl implements LoginLogService {
    
    @Resource
    private LoginLogMapper loginLogMapper;
    
    @Override
    public PageResult<LoginLogDO> getLoginLogPage(LoginLogPageReqVO reqVO) {
        return loginLogMapper.selectPage(reqVO);
    }

    @Override
    public void createLoginLog(LoginLogCreateReqDTO reqDTO) {
        LoginLogDO loginLog = LoginLogConvert.INSTANCE.convert(reqDTO);
        loginLogMapper.insert(loginLog);
    }
}
