package cn.zjh.kayson.module.system.service.user;

import cn.hutool.core.util.StrUtil;
import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.user.vo.user.UserCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.user.vo.user.UserPageReqVO;
import cn.zjh.kayson.module.system.controller.admin.user.vo.user.UserUpdateReqVO;
import cn.zjh.kayson.module.system.convert.user.UserConvert;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import cn.zjh.kayson.module.system.dal.mysql.user.AdminUserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cn.zjh.kayson.framework.common.exception.util.ServiceExceptionUtils.exception;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;

/**
 * 后台用户 Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Resource
    private AdminUserMapper adminUserMapper;
    
    @Override
    public Long createUser(UserCreateReqVO reqVO) {
        // 校验正确性
        validateUserForCreateOrUpdate(null, reqVO.getUsername(), reqVO.getMobile(), reqVO.getEmail(),
                reqVO.getDeptId(), reqVO.getPostIds());
        // 插入用户
        AdminUserDO user = UserConvert.INSTANCE.convert(reqVO);
        user.setStatus(CommonStatusEnum.ENABLE.getValue()); // 默认开启
        user.setPassword(reqVO.getPassword()); // TODO：加密密码
        adminUserMapper.insert(user);
        // TODO: 插入关联岗位
        return user.getId();
    }

    @Override
    public void updateUser(UserUpdateReqVO reqVO) {
        // 校验正确性
        validateUserForCreateOrUpdate(reqVO.getId(), reqVO.getUsername(), reqVO.getMobile(), reqVO.getEmail(), 
                reqVO.getDeptId(), reqVO.getPostIds());
        // 更新用户
        AdminUserDO updateObj = UserConvert.INSTANCE.convert(reqVO);
        adminUserMapper.updateById(updateObj);
        // TODO: 更新岗位
    }

    @Override
    public void deleteUser(Long id) {
        validateUserExists(id);
        // 删除用户
        adminUserMapper.deleteById(id);
        // TODO: 删除用户关联数据
        // TODO: 删除用户岗位
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
        updateObj.setId(id).setPassword(password); // TODO: 加密密码
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
        // TODO: 查询子部门
        Set<Long> deptIds = new HashSet<>();
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
    }

    private void validateUserExists(Long id) {
        if (id == null) {
            return;
        }
        AdminUserDO user = adminUserMapper.selectById(id);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
    
    }
    private void validateUsernameUnique(Long id, String username) {
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

    private void validateMobileUnique(Long id, String mobile) {
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
    
    private void validateEmailUnique(Long id, String email) {
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
}
