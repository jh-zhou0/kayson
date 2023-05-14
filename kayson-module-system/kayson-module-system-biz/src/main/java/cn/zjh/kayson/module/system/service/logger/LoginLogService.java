package cn.zjh.kayson.module.system.service.logger;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.api.logger.dto.LoginLogCreateReqDTO;
import cn.zjh.kayson.module.system.controller.admin.logger.vo.loginlog.LoginLogPageReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.logger.LoginLogDO;

/**
 * 登录日志 Service 接口
 * 
 * @author zjh - kayson
 */
public interface LoginLogService {

    /**
     * 获得登录日志分页
     *
     * @param reqVO 分页条件
     * @return 登录日志分页
     */
    PageResult<LoginLogDO> getLoginLogPage(LoginLogPageReqVO reqVO);

    /**
     * 创建登录日志
     *
     * @param reqDTO 日志信息
     */
    void createLoginLog(LoginLogCreateReqDTO reqDTO);
}
