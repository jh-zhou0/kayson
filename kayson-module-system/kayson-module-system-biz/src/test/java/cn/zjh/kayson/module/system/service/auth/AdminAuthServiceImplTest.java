package cn.zjh.kayson.module.system.service.auth;

import cn.hutool.core.util.ReflectUtil;
import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.enums.UserTypeEnum;
import cn.zjh.kayson.framework.test.core.ut.BaseDbUnitTest;
import cn.zjh.kayson.module.system.controller.admin.auth.vo.AuthLoginReqVO;
import cn.zjh.kayson.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import cn.zjh.kayson.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import cn.zjh.kayson.module.system.enums.logger.LoginLogTypeEnum;
import cn.zjh.kayson.module.system.enums.logger.LoginResultEnum;
import cn.zjh.kayson.module.system.service.logger.LoginLogService;
import cn.zjh.kayson.module.system.service.oauth2.OAuth2TokenService;
import cn.zjh.kayson.module.system.service.user.AdminUserService;
import com.xingyuv.captcha.model.common.ResponseModel;
import com.xingyuv.captcha.service.CaptchaService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.randomPojo;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.randomString;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author zjh - kayson
 */
@Import(AdminAuthServiceImpl.class) // 导入 AdminAuthServiceImpl Bean
public class AdminAuthServiceImplTest extends BaseDbUnitTest {
    
    @Resource // 注入要测试的 AdminAuthServiceImpl Bean
    private AdminAuthServiceImpl authService;
    
    @MockBean // 模拟外部的 AdminUserService Bean
    private AdminUserService adminUserService;
    @MockBean
    private OAuth2TokenService oauth2TokenService;
    @MockBean
    private LoginLogService loginLogService;
    @MockBean
    private CaptchaService captchaService;
    @MockBean
    private Validator validator;
    
    @BeforeEach
    public void setUp() {
        // 注入 captchaEnable 的值，mock 验证码默认打开
        ReflectUtil.setFieldValue(authService, "captchaEnable", true);
        // 注入一个 Validator 对象
        ReflectUtil.setFieldValue(authService, "validator", 
                Validation.buildDefaultValidatorFactory().getValidator());
    }

    @Test
    void testAuthenticate_success() {
        // 准备参数
        String username = randomString();
        String password = randomString();
        // mock user 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setUsername(username)
                .setPassword(password).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        Mockito.when(adminUserService.getUserByUsername(ArgumentMatchers.eq(username))).thenReturn(user);
        // mock password 匹配
        Mockito.when(adminUserService.isPasswordMatch(ArgumentMatchers.eq(password), 
                ArgumentMatchers.eq(user.getPassword()))).thenReturn(true);
        // 调用
        AdminUserDO loginUser = authService.authenticate(username, password);
        // 校验
        assertPojoEquals(user, loginUser);
    }

    @Test
    void testAuthenticate_userNotFound() {
        // 准备参数
        String username = randomString();
        String password = randomString();
        
        // 调用, 并断言异常
        assertServiceException(() -> authService.authenticate(username, password), AUTH_LOGIN_BAD_CREDENTIALS);
        Mockito.verify(loginLogService).createLoginLog(
                ArgumentMatchers.argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType()) 
                        && o.getResult().equals(LoginResultEnum.BAD_CREDENTIALS.getResult())
                        && o.getUserId() == null)
        );
    }

    @Test
    void testAuthenticate_userDisabled() {
        // 准备参数
        String username = randomString();
        String password = randomString();
        // mock user 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setUsername(username)
                .setPassword(password).setStatus(CommonStatusEnum.DISABLE.getStatus()));
        when(adminUserService.getUserByUsername(eq(username))).thenReturn(user);
        // mock password 匹配
        when(adminUserService.isPasswordMatch(eq(password), eq(user.getPassword()))).thenReturn(true);

        // 调用, 并断言异常
        assertServiceException(() -> authService.authenticate(username, password), AUTH_LOGIN_USER_DISABLED);
        verify(loginLogService).createLoginLog(
                argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                        && o.getResult().equals(LoginResultEnum.USER_DISABLED.getResult())
                        && o.getUserId().equals(user.getId()))
        );
    }

    @Test
    void testLogin_success() {
        // 准备参数
        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class, o -> 
                o.setUsername("test_username").setPassword("test_password"));
        // mock 验证码正确
        ReflectUtil.setFieldValue(authService, "captchaEnable", false);
        // mock user 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setId(1L).setUsername("test_username")
                .setPassword("test_password").setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(adminUserService.getUserByUsername(eq("test_username"))).thenReturn(user);
        // mock password 匹配
        when(adminUserService.isPasswordMatch(eq("test_password"), eq(user.getPassword()))).thenReturn(true);
        // mock 缓存登录用户到 Redis
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class, o -> o.setId(1L)
                .setUserType(UserTypeEnum.ADMIN.getValue()));
        when(oauth2TokenService.createAccessToken(eq(1L), eq(UserTypeEnum.ADMIN.getValue()), 
                eq("default"), isNull())).thenReturn(accessTokenDO);

        // 调用，并校验
        AuthLoginRespVO loginRespVO = authService.login(reqVO);
        assertPojoEquals(accessTokenDO, loginRespVO);
        // 校验调用参数
        verify(loginLogService).createLoginLog(
                argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                        && o.getResult().equals(LoginResultEnum.SUCCESS.getResult()) 
                        && o.getUserId().equals(user.getId()))
        );
    }

    @Test
    void testValidateCaptcha_successWithEnable() {
        // 准备参数
        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);

        // mock 验证通过
        when(captchaService.verification(argThat(captchaVO -> {
            Assertions.assertEquals(reqVO.getCaptchaVerification(), captchaVO.getCaptchaVerification());
            return true;
        }))).thenReturn(ResponseModel.success());

        // 调用，无需断言
        authService.validateCaptcha(reqVO);
    }

    @Test
    void testValidateCaptcha_successWithDisable() {
        // 准备参数
        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);
        // 验证码关闭
        ReflectUtil.setFieldValue(authService, "captchaEnable", false);
        // 调用，无需断言
        authService.validateCaptcha(reqVO);
    }

    @Test
    void testValidateCaptcha_constraintViolationException() {
        // 准备参数
        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);
        reqVO.setCaptchaVerification(null);

        // 调用，并断言异常
        Assertions.assertThrows(ConstraintViolationException.class, () -> 
                        authService.validateCaptcha(reqVO), "验证码不能为空");
    }

    @Test
    void testCaptcha_fail() {
        // 准备参数
        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);

        // mock 验证通过
        when(captchaService.verification(argThat(captchaVO -> {
            Assertions.assertEquals(reqVO.getCaptchaVerification(), captchaVO.getCaptchaVerification());
            return true;
        }))).thenReturn(ResponseModel.errorMsg("就是不对"));

        // 调用, 并断言异常
        assertServiceException(() -> authService.validateCaptcha(reqVO), AUTH_LOGIN_CAPTCHA_CODE_ERROR, "就是不对");
        // 校验调用参数
        verify(loginLogService).createLoginLog(
                argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType()) 
                        && o.getResult().equals(LoginResultEnum.CAPTCHA_CODE_ERROR.getResult()))
        );
    }

    @Test
    void testRefreshToken() {
        // 准备参数
        String refreshToken = randomString();
        // mock accessTokenDO
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class);
        when(oauth2TokenService.refreshAccessToken(eq(refreshToken), eq("default"))).thenReturn(accessTokenDO);

        // 调用
        AuthLoginRespVO loginRespVO = authService.refreshToken(refreshToken);
        // 断言
        assertPojoEquals(accessTokenDO, loginRespVO);
    }

    @Test
    void testLogout_success() {
        // 准备参数
        String token = randomString();
        // mock
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class, o -> o.setUserId(1L)
                .setUserType(UserTypeEnum.ADMIN.getValue()));
        when(oauth2TokenService.removeAccessToken(eq(token))).thenReturn(accessTokenDO);

        // 调用
        authService.logout(token, LoginLogTypeEnum.LOGOUT_SELF.getType());
        // 校验调用参数
        verify(loginLogService).createLoginLog(
                argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGOUT_SELF.getType())
                        && o.getResult().equals(LoginResultEnum.SUCCESS.getResult()))
        );
    }

    @Test
    void testLogout_fail() {
        // 准备参数
        String token = randomString();

        // 调用
        authService.logout(token, LoginLogTypeEnum.LOGOUT_SELF.getType());
        // 校验调用参数
        verify(loginLogService, never()).createLoginLog(any());
    }
}
