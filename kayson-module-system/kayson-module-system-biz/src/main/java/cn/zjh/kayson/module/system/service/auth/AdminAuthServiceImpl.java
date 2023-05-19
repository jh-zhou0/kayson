package cn.zjh.kayson.module.system.service.auth;

import cn.hutool.core.util.ObjectUtil;
import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.enums.UserTypeEnum;
import cn.zjh.kayson.framework.common.util.servlet.ServletUtils;
import cn.zjh.kayson.framework.common.util.validation.ValidationUtils;
import cn.zjh.kayson.module.system.api.logger.dto.LoginLogCreateReqDTO;
import cn.zjh.kayson.module.system.controller.admin.auth.vo.AuthLoginReqVO;
import cn.zjh.kayson.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import cn.zjh.kayson.module.system.convert.auth.AuthConvert;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import cn.zjh.kayson.module.system.enums.logger.LoginLogTypeEnum;
import cn.zjh.kayson.module.system.enums.logger.LoginResultEnum;
import cn.zjh.kayson.module.system.enums.oauth2.OAuth2ClientConstants;
import cn.zjh.kayson.module.system.service.logger.LoginLogService;
import cn.zjh.kayson.module.system.service.oauth2.OAuth2TokenService;
import cn.zjh.kayson.module.system.service.user.AdminUserService;
import com.google.common.annotations.VisibleForTesting;
import com.xingyuv.captcha.model.common.ResponseModel;
import com.xingyuv.captcha.model.vo.CaptchaVO;
import com.xingyuv.captcha.service.CaptchaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Validator;

import java.util.Objects;

import static cn.zjh.kayson.framework.common.exception.util.ServiceExceptionUtils.exception;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;

/**
 * Auth Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
public class AdminAuthServiceImpl implements AdminAuthService {
    
    @Resource
    private AdminUserService adminUserService;
    
    @Resource
    private OAuth2TokenService oAuth2TokenService; 
    
    @Resource
    private LoginLogService loginLogService;

    @Resource
    private Validator validator;
    
    @Resource
    private CaptchaService captchaService;
    
    /**
     * 验证码的开关，默认为 true
     */
    @Value("${kayson.captcha.enable:true}")
    private Boolean captchaEnable;

    @Override
    public AuthLoginRespVO login(AuthLoginReqVO reqVO) {
        // 校验验证码
        validateCaptcha(reqVO);
        // 使用账号密码，进行登录
        AdminUserDO user = authenticate(reqVO.getUsername(), reqVO.getPassword());
        // TODO: 绑定社交用户
        // 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(user.getId(), user.getUsername(), LoginLogTypeEnum.LOGIN_USERNAME);
    }

    @VisibleForTesting
    void validateCaptcha(AuthLoginReqVO reqVO) {
        // 如果验证码关闭，则不进行校验
        if (!captchaEnable) {
            return;
        }
        // 校验验证码
        ValidationUtils.validate(validator, reqVO, AuthLoginReqVO.CodeEnableGroup.class);
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(reqVO.getCaptchaVerification());
        ResponseModel responseModel = captchaService.verification(captchaVO);
        // 验证不通过
        if (!responseModel.isSuccess()) {
            // 创建登录失败日志（验证码不正确)
            createLoginLog(null, reqVO.getUsername(), LoginLogTypeEnum.LOGIN_USERNAME, LoginResultEnum.CAPTCHA_CODE_ERROR);
            throw exception(AUTH_LOGIN_CAPTCHA_CODE_ERROR, responseModel.getRepMsg());
        }
    }

    @Override
    public AdminUserDO authenticate(String username, String password) {
        final LoginLogTypeEnum loginType = LoginLogTypeEnum.LOGIN_USERNAME;
        // 校验账号是否存在
        AdminUserDO user = adminUserService.getUserByUsername(username);
        if (user == null) {
            createLoginLog(null, username, loginType, LoginResultEnum.BAD_CREDENTIALS);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        }
        // 校验密码是否正确
        if (!adminUserService.isPasswordMatch(password, user.getPassword())) {
            createLoginLog(user.getId(), username, loginType, LoginResultEnum.BAD_CREDENTIALS);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        }
        // 校验是否禁用
        if (ObjectUtil.notEqual(user.getStatus(), CommonStatusEnum.ENABLE.getValue())) {
            createLoginLog(user.getId(), username, loginType, LoginResultEnum.USER_DISABLED);
            throw exception(AUTH_LOGIN_USER_DISABLED);
        }
        return user;
    }

    @Override
    public void logout(String token, Integer logoutType) {
        // 删除访问令牌
        OAuth2AccessTokenDO accessTokenDO = oAuth2TokenService.removeAccessToken(token);
        if (accessTokenDO == null) {
            return;
        }
        // 删除成功，则记录登出日志
        createLogoutLog(accessTokenDO.getUserId(), accessTokenDO.getUserType(), logoutType);
    }

    @Override
    public AuthLoginRespVO refreshToken(String refreshToken) {
        OAuth2AccessTokenDO accessTokenDO = oAuth2TokenService.refreshAccessToken(refreshToken, OAuth2ClientConstants.CLIENT_ID_DEFAULT);
        return AuthConvert.INSTANCE.convert(accessTokenDO);
    }

    private AuthLoginRespVO createTokenAfterLoginSuccess(Long userId, String username, LoginLogTypeEnum loginType) {
        // 插入登陆日志
        createLoginLog(userId, username, loginType, LoginResultEnum.SUCCESS);
        // 创建访问令牌
        OAuth2AccessTokenDO accessToken = oAuth2TokenService.createAccessToken(userId, getUserType().getValue(),
                OAuth2ClientConstants.CLIENT_ID_DEFAULT, null);
        // 构建返回结果
        return AuthConvert.INSTANCE.convert(accessToken);
    }

    private void createLoginLog(Long userId, String username, LoginLogTypeEnum loginType, LoginResultEnum loginResult) {
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO()
                .setLogType(loginType.getType())
                .setTraceId(null) // TODO: SkyWalking链路追踪
                .setUserId(userId)
                .setUserType(getUserType().getValue())
                .setUsername(username)
                .setUserAgent(ServletUtils.getUserAgent())
                .setUserIp(ServletUtils.getClientIP())
                .setResult(loginResult.getResult());
        loginLogService.createLoginLog(reqDTO);
        // 更新最后登录时间
        if (userId != null && Objects.equals(LoginResultEnum.SUCCESS.getResult(), loginResult.getResult())) {
            adminUserService.updateUserLogin(userId, ServletUtils.getClientIP());
        }
    }

    private void createLogoutLog(Long userId, Integer userType, Integer logoutType) {
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO()
                .setLogType(logoutType)
                .setTraceId(null)
                .setUserId(userId)
                .setUserType(getUserType().getValue())
                .setUsername(getUsername(userId))
                .setUserAgent(ServletUtils.getUserAgent())
                .setUserIp(ServletUtils.getClientIP())
                .setResult(LoginResultEnum.SUCCESS.getResult());
        loginLogService.createLoginLog(reqDTO);
    }

    private String getUsername(Long userId) {
        if (userId == null) {
            return null;
        }
        AdminUserDO user = adminUserService.getUser(userId);
        return user == null ? null : user.getUsername();
    }

    private UserTypeEnum getUserType() {
        return UserTypeEnum.ADMIN;
    }
}
