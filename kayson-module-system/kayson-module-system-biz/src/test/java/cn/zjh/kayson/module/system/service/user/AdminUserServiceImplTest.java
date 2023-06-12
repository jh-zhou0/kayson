package cn.zjh.kayson.module.system.service.user;

import cn.hutool.core.util.RandomUtil;
import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.exception.ServiceException;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.test.core.ut.BaseDbUnitTest;
import cn.zjh.kayson.module.infra.api.FileApi;
import cn.zjh.kayson.module.system.controller.admin.user.vo.profile.UserProfileUpdatePasswordReqVO;
import cn.zjh.kayson.module.system.controller.admin.user.vo.profile.UserProfileUpdateReqVO;
import cn.zjh.kayson.module.system.controller.admin.user.vo.user.*;
import cn.zjh.kayson.module.system.dal.dataobject.dept.DeptDO;
import cn.zjh.kayson.module.system.dal.dataobject.dept.UserPostDO;
import cn.zjh.kayson.module.system.dal.dataobject.tenant.TenantDO;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import cn.zjh.kayson.module.system.dal.mysql.dept.UserPostMapper;
import cn.zjh.kayson.module.system.dal.mysql.user.AdminUserMapper;
import cn.zjh.kayson.module.system.enums.common.SexEnum;
import cn.zjh.kayson.module.system.service.dept.DeptService;
import cn.zjh.kayson.module.system.service.dept.PostService;
import cn.zjh.kayson.module.system.service.permission.PermissionService;
import cn.zjh.kayson.module.system.service.tenant.TenantService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;

import static cn.hutool.core.util.RandomUtil.randomBytes;
import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.zjh.kayson.framework.common.util.collection.SetUtils.asSet;
import static cn.zjh.kayson.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.zjh.kayson.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.zjh.kayson.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.*;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author zjh - kayson
 */
@Import(AdminUserServiceImpl.class)
public class AdminUserServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AdminUserServiceImpl adminUserService;
    @Resource
    private AdminUserMapper adminUserMapper;
    @Resource
    private UserPostMapper userPostMapper;
    
    @MockBean
    private DeptService deptService;
    @MockBean
    private PostService postService;
    @MockBean
    private PermissionService permissionService;
    @MockBean
    private TenantService tenantService;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private FileApi fileApi;

    @Test
    void testCreatUser_success() {
        // 准备参数
        UserCreateReqVO reqVO = randomPojo(UserCreateReqVO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setMobile(randomString());
            o.setPostIds(asSet(1L, 2L));
        });
        // mock passwordEncoder 的方法
        when(passwordEncoder.encode(eq(reqVO.getPassword()))).thenReturn("kayson");
        // mock 账户额度充足
        TenantDO tenant = randomPojo(TenantDO.class, o -> o.setAccountCount(1));
        doNothing().when(tenantService).handleTenantInfo(argThat(handler -> {
            handler.handle(tenant);
            return true;
        }));
        
        // 调用
        Long userId = adminUserService.createUser(reqVO);
        // 断言
        AdminUserDO user = adminUserMapper.selectById(userId);
        assertPojoEquals(reqVO, user, "password");
        assertEquals("kayson", user.getPassword());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), user.getStatus());
        // 断言关联岗位
        List<UserPostDO> userPosts = userPostMapper.selectListByUserId(userId);
        assertEquals(1L, userPosts.get(0).getPostId());
        assertEquals(2L, userPosts.get(1).getPostId());
    }

    @Test
    void testUpdateUser_success() {
        // mock 数据
        AdminUserDO dbUser = randomPojo(AdminUserDO.class, o -> 
                o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex()).setPostIds(asSet(1L, 2L)));
        adminUserMapper.insert(dbUser);
        userPostMapper.insert(new UserPostDO().setUserId(dbUser.getId()).setPostId(1L));
        userPostMapper.insert(new UserPostDO().setUserId(dbUser.getId()).setPostId(2L));
        // 准备参数
        UserUpdateReqVO reqVO = randomPojo(UserUpdateReqVO.class, o -> {
            o.setId(dbUser.getId());
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setMobile(randomString());
            o.setPostIds(asSet(2L, 3L));
        });

        // 调用
        adminUserService.updateUser(reqVO);
        // 断言
        AdminUserDO user = adminUserMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, user);
        // 断言关联岗位
        List<UserPostDO> userPosts = userPostMapper.selectListByUserId(reqVO.getId());
        assertEquals(2L, userPosts.get(0).getPostId());
        assertEquals(3L, userPosts.get(1).getPostId());
    }

    @Test
    void testDeleteUser_success() {
        // mock 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        adminUserMapper.insert(user);
        // 准备参数
        Long userId = user.getId();
        
        // 调用
        adminUserService.deleteUser(userId);
        // 断言
        assertNull(adminUserMapper.selectById(userId));
        // 校验调用次数
        verify(permissionService, times(1)).processUserDeleted(userId);
        
    }

    @Test
    void testGetUser() {
        // mock 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        adminUserMapper.insert(user);
        // 准备参数
        Long userId = user.getId();

        // 调用
        AdminUserDO userDO = adminUserService.getUser(userId);
        // 断言
        assertPojoEquals(user, userDO);
    }

    @Test
    void testGetUserPage() {
        // mock 数据
        AdminUserDO user = initGetUserPageData();

        // 准备参数
        UserPageReqVO reqVO = new UserPageReqVO();
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setUsername("kay");
        reqVO.setMobile("189");
        reqVO.setCreateTime(buildBetweenTime(2023, 5, 21, 2023, 5, 23));
        reqVO.setDeptId(1L); // 其中，1L 是 2L 的父部门

        // mock 方法
        List<DeptDO> deptDOList = Collections.singletonList(randomPojo(DeptDO.class, o -> o.setId(2L)));
        when(deptService.getDeptListByParentIdFromCache(eq(reqVO.getDeptId()), eq(true))).thenReturn(deptDOList);
        
        // 调用
        PageResult<AdminUserDO> pageResult = adminUserService.getUserPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(user, pageResult.getList().get(0));
    }

    @Test
    public void testGetUserList_export() {
        // mock 数据
        AdminUserDO dbUser = initGetUserPageData();
        // 准备参数
        UserExportReqVO reqVO = new UserExportReqVO();
        reqVO.setUsername("kay");
        reqVO.setMobile("18988");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime(buildBetweenTime(2023, 5, 21, 2023, 5, 31));
        reqVO.setDeptId(1L); // 其中，1L 是 2L 的父部门
        // mock 方法
        List<DeptDO> deptList = newArrayList(randomPojo(DeptDO.class, o -> o.setId(2L)));
        when(deptService.getDeptListByParentIdFromCache(eq(reqVO.getDeptId()), eq(true))).thenReturn(deptList);

        // 调用
        List<AdminUserDO> list = adminUserService.getUserList(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbUser, list.get(0));
    }

    private AdminUserDO initGetUserPageData() {
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setUsername("kayson");
            o.setMobile("18988998899");
            o.setCreateTime(buildTime(2023, 5, 22));
            o.setDeptId(2L);
        });
        adminUserMapper.insert(user);
        // 测试 status 不匹配
        adminUserMapper.insert(cloneIgnoreId(user, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 测试 username 不匹配
        adminUserMapper.insert(cloneIgnoreId(user, o -> o.setUsername("zjh")));
        // 测试 mobile 不匹配
        adminUserMapper.insert(cloneIgnoreId(user, o -> o.setMobile("11911001100")));
        // 测试 createTime 不匹配
        adminUserMapper.insert(cloneIgnoreId(user, o -> o.setCreateTime(buildTime(2023, 5, 20))));
        // 测试 dept 不匹配
        adminUserMapper.insert(cloneIgnoreId(user, o -> o.setDeptId(0L)));
        return user;
    }

    @Test
    void testUpdateUserPassword_success_01() {
        // mock 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        adminUserMapper.insert(user);
        // 准备参数
        Long id = user.getId();
        String password = "kayson";

        // mock 方法
        when(passwordEncoder.encode(anyString())).then(invocation -> "encode:" + invocation.getArgument(0));
        
        // 调用
        adminUserService.updateUserPassword(id, password);
        // 断言
        AdminUserDO userDO = adminUserMapper.selectById(id);
        assertEquals("encode:" + password, userDO.getPassword());
    }

    @Test
    void testUpdateUserStatus() {
        // mock 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        adminUserMapper.insert(user);
        // 准备参数
        Long id = user.getId();
        Integer status = randomCommonStatus();
        
        // 调用
        adminUserService.updateUserStatus(id, status);
        // 断言
        AdminUserDO userDO = adminUserMapper.selectById(id);
        assertEquals(status, userDO.getStatus());
    }

    @Test
    void testUpdateUserLogin() {
        // mock 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> {
           o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
           o.setStatus(CommonStatusEnum.ENABLE.getStatus());
           o.setLoginDate(null);
        });
        adminUserMapper.insert(user);
        // 准备参数
        Long id = user.getId();
        String loginIp = randomString();
        
        // 调用
        adminUserService.updateUserLogin(id, loginIp);
        // 断言
        AdminUserDO userDO = adminUserMapper.selectById(id);
        assertEquals(loginIp, userDO.getLoginIp());
        assertNotNull(userDO.getLoginDate());
    }

    @Test
    void testUpdateUserProfile_success() {
        // mock 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        adminUserMapper.insert(user);
        // 准备参数
        Long id = user.getId();
        UserProfileUpdateReqVO reqVO = randomPojo(UserProfileUpdateReqVO.class, o -> {
           o.setMobile(randomString());
           o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
        });
        
        // 调用
        adminUserService.updateUserProfile(id, reqVO);
        // 断言
        AdminUserDO userDO = adminUserMapper.selectById(id);
        assertPojoEquals(reqVO, userDO);
    }

    @Test
    void testUpdateUserPassword_success_02() {
        // mock 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setPassword("encode:kayson");
        });
        adminUserMapper.insert(user);
        // 准备参数
        Long id = user.getId();
        UserProfileUpdatePasswordReqVO reqVO = randomPojo(UserProfileUpdatePasswordReqVO.class, o -> {
           o.setOldPassword("kayson"); 
           o.setNewPassword("new_password");
        });
        // mock 方法
        when(passwordEncoder.encode(anyString())).then(invocation -> "encode:" + invocation.getArgument(0));
        when(passwordEncoder.matches(eq(reqVO.getOldPassword()), eq(user.getPassword()))).thenReturn(true);

        // 调用
        adminUserService.updateUserPassword(id, reqVO);
        // 断言
        AdminUserDO userDO = adminUserMapper.selectById(id);
        assertEquals("encode:new_password", userDO.getPassword());
    }

    @Test
    public void testUpdateUserAvatar_success() {
        // mock 数据
        AdminUserDO dbUser = randomPojo(AdminUserDO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setStatus(randomCommonStatus());
        });
        adminUserMapper.insert(dbUser);
        // 准备参数
        Long userId = dbUser.getId();
        byte[] avatarFileBytes = randomBytes(10);
        ByteArrayInputStream avatarFile = new ByteArrayInputStream(avatarFileBytes);
        // mock 方法
        String avatar = randomString();
        when(fileApi.createFile(eq(avatarFileBytes))).thenReturn(avatar);

        // 调用
        adminUserService.updateUserAvatar(userId, avatarFile);
        // 断言
        AdminUserDO user = adminUserMapper.selectById(userId);
        assertEquals(avatar, user.getAvatar());
    }

    @Test
    void testGetUserByUsername() {
        // mock 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        adminUserMapper.insert(user);
        // 准备参数
        String username = user.getUsername();
        
        // 调用
        AdminUserDO userDO = adminUserService.getUserByUsername(username);
        // 断言
        assertPojoEquals(user, userDO);
    }

    /**
     * 情况一，校验不通过，导致插入失败
     */
    @Test
    public void testImportUserList_01() {
        // 准备参数
        UserImportExcelVO importUser = randomPojo(UserImportExcelVO.class);
        // mock 方法，模拟失败
        doThrow(new ServiceException(DEPT_NOT_FOUND)).when(deptService).validateDeptList(any());

        // 调用
        UserImportRespVO respVO = adminUserService.importUserList(newArrayList(importUser), true);
        // 断言
        assertEquals(0, respVO.getCreateUsernames().size());
        assertEquals(0, respVO.getUpdateUsernames().size());
        assertEquals(1, respVO.getFailureUsernames().size());
        assertEquals(DEPT_NOT_FOUND.getMsg(), respVO.getFailureUsernames().get(importUser.getUsername()));
    }

    /**
     * 情况二，不存在，进行插入
     */
    @Test
    public void testImportUserList_02() {
        // 准备参数
        UserImportExcelVO importUser = randomPojo(UserImportExcelVO.class, o -> {
            o.setStatus(randomCommonStatus()); // 保证 status 的范围
            o.setSex(randomEle(SexEnum.values()).getSex()); // 保证 sex 的范围
        });
        // mock deptService 的方法
        DeptDO dept = randomPojo(DeptDO.class, o -> {
            o.setId(importUser.getDeptId());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(deptService.getDept(eq(dept.getId()))).thenReturn(dept);
        // mock passwordEncoder 的方法
        when(passwordEncoder.encode(eq("123456"))).thenReturn("java");

        // 调用
        UserImportRespVO respVO = adminUserService.importUserList(newArrayList(importUser), true);
        // 断言
        assertEquals(1, respVO.getCreateUsernames().size());
        AdminUserDO user = adminUserMapper.selectByUsername(respVO.getCreateUsernames().get(0));
        assertPojoEquals(importUser, user);
        assertEquals("java", user.getPassword());
        assertEquals(0, respVO.getUpdateUsernames().size());
        assertEquals(0, respVO.getFailureUsernames().size());
    }

    /**
     * 情况三，存在，但是不强制更新
     */
    @Test
    public void testImportUserList_03() {
        // mock 数据
        AdminUserDO dbUser = randomPojo(AdminUserDO.class, o -> {
            o.setStatus(randomCommonStatus()); // 保证 status 的范围
            o.setSex(randomEle(SexEnum.values()).getSex()); // 保证 sex 的范围
        });
        adminUserMapper.insert(dbUser);
        // 准备参数
        UserImportExcelVO importUser = randomPojo(UserImportExcelVO.class, o -> {
            o.setStatus(randomCommonStatus()); // 保证 status 的范围
            o.setSex(randomEle(SexEnum.values()).getSex()); // 保证 sex 的范围
            o.setUsername(dbUser.getUsername());
        });
        // mock deptService 的方法
        DeptDO dept = randomPojo(DeptDO.class, o -> {
            o.setId(importUser.getDeptId());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(deptService.getDept(eq(dept.getId()))).thenReturn(dept);

        // 调用
        UserImportRespVO respVO = adminUserService.importUserList(newArrayList(importUser), false);
        // 断言
        assertEquals(0, respVO.getCreateUsernames().size());
        assertEquals(0, respVO.getUpdateUsernames().size());
        assertEquals(1, respVO.getFailureUsernames().size());
        assertEquals(USER_USERNAME_EXISTS.getMsg(), respVO.getFailureUsernames().get(importUser.getUsername()));
    }

    /**
     * 情况四，存在，强制更新
     */
    @Test
    public void testImportUserList_04() {
        // mock 数据
        AdminUserDO dbUser = randomPojo(AdminUserDO.class, o -> {
            o.setStatus(randomCommonStatus()); // 保证 status 的范围
            o.setSex(randomEle(SexEnum.values()).getSex()); // 保证 sex 的范围
        });
        adminUserMapper.insert(dbUser);
        // 准备参数
        UserImportExcelVO importUser = randomPojo(UserImportExcelVO.class, o -> {
            o.setStatus(randomCommonStatus()); // 保证 status 的范围
            o.setSex(randomEle(SexEnum.values()).getSex()); // 保证 sex 的范围
            o.setUsername(dbUser.getUsername());
        });
        // mock deptService 的方法
        DeptDO dept = randomPojo(DeptDO.class, o -> {
            o.setId(importUser.getDeptId());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(deptService.getDept(eq(dept.getId()))).thenReturn(dept);

        // 调用
        UserImportRespVO respVO = adminUserService.importUserList(newArrayList(importUser), true);
        // 断言
        assertEquals(0, respVO.getCreateUsernames().size());
        assertEquals(1, respVO.getUpdateUsernames().size());
        AdminUserDO user = adminUserMapper.selectByUsername(respVO.getUpdateUsernames().get(0));
        assertPojoEquals(importUser, user);
        assertEquals(0, respVO.getFailureUsernames().size());
    }

    @Test
    void testValidateUserExists_notExists() {
        assertServiceException(() -> adminUserService.validateUserExists(randomLong()), USER_NOT_EXISTS);
    }

    @Test
    void testValidateUsernameUnique_usernameExistsForCreate() {
        // 准备参数
        String username = randomString();
        // mock 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setUsername(username);
        });
        adminUserMapper.insert(user);

        // 调用，校验异常
        assertServiceException(() -> adminUserService.validateUsernameUnique(null, username), USER_USERNAME_EXISTS);
    }

    @Test
    void testValidateUsernameUnique_usernameExistsForUpdate() {
        // 准备参数
        String username = randomString();
        // mock 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setUsername(username);
        });
        adminUserMapper.insert(user);

        // 调用，校验异常
        assertServiceException(() -> adminUserService.validateUsernameUnique(randomLong(), username), USER_USERNAME_EXISTS);
    }

    @Test
    void testValidateEmailUnique_emailExistsForCreate() {
        // 准备参数
        String email = randomString();
        // mock 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setEmail(email);
        });
        adminUserMapper.insert(user);

        // 调用，校验异常
        assertServiceException(() -> adminUserService.validateEmailUnique(null, email), USER_EMAIL_EXISTS);
    }

    @Test
    void testValidateEmailUnique_emailExistsForUpdate() {
        // 准备参数
        String email = randomString();
        // mock 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setEmail(email);
        });
        adminUserMapper.insert(user);

        // 调用，校验异常
        assertServiceException(() -> adminUserService.validateEmailUnique(randomLong(), email), USER_EMAIL_EXISTS);
    }

    @Test
    void testValidateMobileUnique_mobileExistsForCreate() {
        // 准备参数
        String mobile = randomString();
        // mock 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setMobile(mobile);
        });
        adminUserMapper.insert(user);

        // 调用，校验异常
        assertServiceException(() -> adminUserService.validateMobileUnique(null, mobile), USER_MOBILE_EXISTS);
    }

    @Test
    void testValidateMobileUnique_mobileExistsForUpdate() {
        // 准备参数
        String mobile = randomString();
        // mock 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setMobile(mobile);
        });
        adminUserMapper.insert(user);

        // 调用，校验异常
        assertServiceException(() -> adminUserService.validateMobileUnique(randomLong(), mobile), USER_MOBILE_EXISTS);
    }

    @Test
    void testValidateOldPassword_notExists() {
        assertServiceException(() -> adminUserService.validateOldPassword(randomLong(), randomString()), USER_NOT_EXISTS);
    }

    @Test
    void testValidateOldPassword_passwordFailed() {
        // mock 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        adminUserMapper.insert(user);
        // 准备参数
        Long id = user.getId();
        String oldPassword = user.getPassword();

        // 调用，校验异常
        assertServiceException(() -> adminUserService.validateOldPassword(id, oldPassword), USER_PASSWORD_FAILED);
        // 校验调用
        verify(passwordEncoder, times(1)).matches(eq(oldPassword), eq(user.getPassword()));
    }
}
