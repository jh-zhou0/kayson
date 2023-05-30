package cn.zjh.kayson.module.system.service.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.exception.ServiceException;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.common.util.collection.CollectionUtils;
import cn.zjh.kayson.module.system.controller.admin.user.vo.profile.UserProfileUpdatePasswordReqVO;
import cn.zjh.kayson.module.system.controller.admin.user.vo.profile.UserProfileUpdateReqVO;
import cn.zjh.kayson.module.system.controller.admin.user.vo.user.*;
import cn.zjh.kayson.module.system.convert.user.UserConvert;
import cn.zjh.kayson.module.system.dal.dataobject.dept.DeptDO;
import cn.zjh.kayson.module.system.dal.dataobject.dept.UserPostDO;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import cn.zjh.kayson.module.system.dal.mysql.dept.UserPostMapper;
import cn.zjh.kayson.module.system.dal.mysql.user.AdminUserMapper;
import cn.zjh.kayson.module.system.service.dept.DeptService;
import cn.zjh.kayson.module.system.service.dept.PostService;
import cn.zjh.kayson.module.system.service.permission.PermissionService;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

import static cn.zjh.kayson.framework.common.exception.util.ServiceExceptionUtils.exception;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;

/**
 * 后台用户 Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Value("${kayson.user.init-password:123456}")
    private String userInitPassword;

    @Resource
    private AdminUserMapper adminUserMapper;
    
    @Resource
    private DeptService deptService;
    
    @Resource
    private PostService postService;
    
    @Resource
    private UserPostMapper userPostMapper;
    
    @Resource
    private PermissionService permissionService;
    
    @Resource
    private PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateReqVO reqVO) {
        // 校验正确性
        validateUserForCreateOrUpdate(null, reqVO.getUsername(), reqVO.getMobile(), reqVO.getEmail(),
                reqVO.getDeptId(), reqVO.getPostIds());
        // 插入用户
        AdminUserDO user = UserConvert.INSTANCE.convert(reqVO);
        user.setStatus(CommonStatusEnum.ENABLE.getStatus()); // 默认开启
        user.setPassword(encodePassword(reqVO.getPassword())); // 加密密码
        adminUserMapper.insert(user);
        // 插入关联岗位
        if (CollUtil.isNotEmpty(user.getPostIds())) {
            List<UserPostDO> userPostDOList = CollectionUtils.convertList(user.getPostIds(), 
                    postId -> new UserPostDO().setUserId(user.getId()).setPostId(postId));
            userPostMapper.insertBatch(userPostDOList);
        }
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateReqVO reqVO) {
        // 校验正确性
        validateUserForCreateOrUpdate(reqVO.getId(), reqVO.getUsername(), reqVO.getMobile(), reqVO.getEmail(), 
                reqVO.getDeptId(), reqVO.getPostIds());
        // 更新用户
        AdminUserDO updateObj = UserConvert.INSTANCE.convert(reqVO);
        adminUserMapper.updateById(updateObj);
        // 更新岗位
        updateUserPost(reqVO, updateObj);
    }

    private void updateUserPost(UserUpdateReqVO reqVO, AdminUserDO updateObj) {
        Long userId = reqVO.getId();
        // 获取当前用户对应的岗位
        List<UserPostDO> userPostDOList = userPostMapper.selectListByUserId(userId);
        Set<Long> dbPostIds = CollectionUtils.convertSet(userPostDOList, UserPostDO::getPostId);
        // 计算新增和删除的岗位编号
        Set<Long> postIds = updateObj.getPostIds();
        Collection<Long> createPostIds = CollUtil.subtract(postIds, dbPostIds);
        Collection<Long> deletePostIds = CollUtil.subtract(dbPostIds, postIds);
        // 执行新增和删除。对于已经授权的菜单，不用做任何处理
        if (CollUtil.isNotEmpty(createPostIds)) {
            userPostMapper.insertBatch(CollectionUtils.convertList(createPostIds, 
                    postId -> new UserPostDO().setUserId(userId).setPostId(postId)));
        }
        if (CollUtil.isNotEmpty(deletePostIds)) {
            userPostMapper.deleteByUserIdAndPostId(userId, deletePostIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        validateUserExists(id);
        // 删除用户
        adminUserMapper.deleteById(id);
        // 删除用户关联数据
        permissionService.processUserDeleted(id);
        // 删除用户岗位
        userPostMapper.deleteByUserId(id);
    }

    @Override
    public AdminUserDO getUser(Long id) {
        return adminUserMapper.selectById(id);
    }

    @Override
    public PageResult<AdminUserDO> getUserPage(UserPageReqVO reqVO) {
        return adminUserMapper.selectPage(reqVO, getDeptCondition(reqVO.getDeptId()));
    }

    @Override
    public void updateUserPassword(Long id, String password) {
        // 校验用户存在
        validateUserExists(id);
        // 更新密码
        AdminUserDO updateObj = new AdminUserDO();
        updateObj.setId(id).setPassword(encodePassword(password)); // 加密密码
        adminUserMapper.updateById(updateObj);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        // 校验用户存在
        validateUserExists(id);
        // 更新状态
        AdminUserDO updateObj = new AdminUserDO();
        updateObj.setId(id).setStatus(status);
        adminUserMapper.updateById(updateObj);
    }

    @Override
    public List<AdminUserDO> getUserListByStatus(Integer status) {
        return adminUserMapper.selectListByStatus(status);
    }

    @Override
    public AdminUserDO getUserByUsername(String username) {
        return adminUserMapper.selectByUsername(username);
    }

    @Override
    public boolean isPasswordMatch(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }

    @Override
    public void updateUserLogin(Long id, String loginIp) {
        adminUserMapper.updateById(new AdminUserDO().setId(id).setLoginIp(loginIp).setLoginDate(LocalDateTime.now()));
    }

    @Override
    public void updateUserProfile(Long id, UserProfileUpdateReqVO reqVO) {
        // 校验正确性
        validateUserExists(id);
        validateEmailUnique(id, reqVO.getEmail());
        validateMobileUnique(id, reqVO.getMobile());
        // 执行更新
        AdminUserDO updateObj = UserConvert.INSTANCE.convert02(reqVO);
        updateObj.setId(id);
        adminUserMapper.updateById(updateObj);
    }

    @Override
    public void updateUserPassword(Long id, UserProfileUpdatePasswordReqVO reqVO) {
        // 校验旧密码
        validateOldPassword(id, reqVO.getOldPassword());
        // 执行更新
        AdminUserDO updateObj = new AdminUserDO();
        updateObj.setId(id);
        updateObj.setPassword(encodePassword(reqVO.getNewPassword()));
        adminUserMapper.updateById(updateObj);
    }

    @Override
    public String updateUserAvatar(Long id, InputStream avatarFile) throws Exception {
        // TODO: 更新用户头像
        return null;
    }

    @Override
    public List<AdminUserDO> getUserListByNickname(String nickname) {
        return adminUserMapper.selectListByNickname(nickname);
    }

    @Override
    public List<AdminUserDO> getUserList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return adminUserMapper.selectBatchIds(ids);
    }

    @Override
    public List<AdminUserDO> getUserList(UserExportReqVO reqVO) {
        return adminUserMapper.selectList(reqVO, getDeptCondition(reqVO.getDeptId()));
    }

    @Override
    public UserImportRespVO importUserList(List<UserImportExcelVO> importUsers, boolean isUpdateSupport) {
        if (CollUtil.isEmpty(importUsers)) {
            throw exception(USER_IMPORT_LIST_IS_EMPTY);
        }
        UserImportRespVO respVO = new UserImportRespVO().setCreateUsernames(new ArrayList<>())
                .setUpdateUsernames(new ArrayList<>()).setFailureUsernames(new LinkedHashMap<>());
        importUsers.forEach(importUser -> {
            // 校验，判断是否存在不符合字段
            try {
                validateUserForCreateOrUpdate(null, null, importUser.getMobile(),
                        importUser.getEmail(), importUser.getDeptId(), null);
            } catch (ServiceException e) {
                respVO.getFailureUsernames().put(importUser.getUsername(), e.getMessage());
                return;
            }
            // 判断如果不存在，再进行插入
            AdminUserDO existUser = adminUserMapper.selectByUsername(importUser.getUsername());
            if (existUser == null) {
                adminUserMapper.insert(UserConvert.INSTANCE.convert(importUser)
                        .setPassword(encodePassword(userInitPassword)).setPostIds(new HashSet<>())); // 设置默认密码及空岗位编号数组
                respVO.getCreateUsernames().add(importUser.getUsername());
                return;
            }
            // 如果存在，判断是否允许更新
            if (!isUpdateSupport) {
                respVO.getFailureUsernames().put(importUser.getUsername(), USER_USERNAME_EXISTS.getMsg());
                return;
            }
            AdminUserDO updateUser = UserConvert.INSTANCE.convert(importUser);
            updateUser.setId(existUser.getId());
            adminUserMapper.updateById(updateUser);
            respVO.getUpdateUsernames().add(importUser.getUsername());
        });
        return respVO;
    }

    @VisibleForTesting
    void validateOldPassword(Long id, String oldPassword) {
        AdminUserDO user = adminUserMapper.selectById(id);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        if (!isPasswordMatch(oldPassword, user.getPassword())) {
            throw exception(USER_PASSWORD_FAILED);
        }
    }

    /**
     * 获得部门条件：查询指定部门的子部门编号们，包括自身
     * 
     * @param deptId 部门编号
     * @return 部门编号集合
     */
    private Set<Long> getDeptCondition(Long deptId) {
        if (deptId == null) {
            return Collections.emptySet();
        }
        // 查询子部门
        List<DeptDO> children = deptService.getDeptListByParentIdFromCache(deptId, true);
        Set<Long> deptIds = CollectionUtils.convertSet(children, DeptDO::getId);
        deptIds.add(deptId); // 包括自身
        return deptIds;
    }

    private void validateUserForCreateOrUpdate(Long id, String username, String mobile, String email,
                                               Long deptId, Set<Long> postIds) {
        // 校验用户存在
        validateUserExists(id);
        // 校验用户名唯一
        validateUsernameUnique(id, username);
        // 校验手机号唯一
        validateMobileUnique(id, mobile);
        // 校验邮箱唯一
        validateEmailUnique(id, email);
        // 校验部门处于开启状态
        deptService.validateDeptList(CollectionUtils.singleton(deptId));
        // 校验岗位处于开启状态
        postService.validatePostList(postIds);
    }

    @VisibleForTesting
    void validateUserExists(Long id) {
        if (id == null) {
            return;
        }
        AdminUserDO user = adminUserMapper.selectById(id);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
    
    }
    
    @VisibleForTesting
    void validateUsernameUnique(Long id, String username) {
        if (StrUtil.isBlank(username)) {
            return;
        }
        AdminUserDO user = adminUserMapper.selectByUsername(username);
        if (user == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的用户
        if (id == null) {
            throw exception(USER_USERNAME_EXISTS);
        }
        if (!id.equals(user.getId())) {
            throw exception(USER_USERNAME_EXISTS);
        }
    }

    @VisibleForTesting
    void validateMobileUnique(Long id, String mobile) {
        if (StrUtil.isBlank(mobile)) {
            return;
        }
        AdminUserDO user = adminUserMapper.selectByMobile(mobile);
        if (user == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的用户
        if (id == null) {
            throw exception(USER_MOBILE_EXISTS);
        }
        if (!id.equals(user.getId())) {
            throw exception(USER_MOBILE_EXISTS);
        }
    }
    
    @VisibleForTesting
    void validateEmailUnique(Long id, String email) {
        if (StrUtil.isBlank(email)) {
            return;
        }
        AdminUserDO user = adminUserMapper.selectByEmail(email);
        if (user == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的用户
        if (id == null) {
            throw exception(USER_EMAIL_EXISTS);
        }
        if (!id.equals(user.getId())) {
            throw exception(USER_EMAIL_EXISTS);
        }
    }

    /**
     * 对密码进行加密
     *
     * @param password 密码
     * @return 加密后的密码
     */
    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
