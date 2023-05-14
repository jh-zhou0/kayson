package cn.zjh.kayson.module.system.service.auth;

import cn.zjh.kayson.module.system.controller.admin.auth.vo.AuthLoginReqVO;
import cn.zjh.kayson.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;

import javax.validation.Valid;

/**
 * 管理后台的认证 Service 接口
 * 
 * @author zjh - kayson
 */
public interface AdminAuthService {
    
    /**
     * 账号登录
     *
     * @param reqVO 登录信息
     * @return 登录结果
     */
    AuthLoginRespVO login(@Valid AuthLoginReqVO reqVO);
    
    /**
     * 验证账号 + 密码。如果通过，则返回用户
     *
     * @param username 账号
     * @param password 密码
     * @return 用户
     */
    AdminUserDO authenticate(String username, String password);
    
}
