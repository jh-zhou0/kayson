package cn.zjh.kayson.module.system.service.user;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.common.util.collection.CollectionUtils;
import cn.zjh.kayson.module.system.controller.admin.user.vo.profile.UserProfileUpdatePasswordReqVO;
import cn.zjh.kayson.module.system.controller.admin.user.vo.profile.UserProfileUpdateReqVO;
import cn.zjh.kayson.module.system.controller.admin.user.vo.user.*;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;

import javax.validation.Valid;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 后台用户 Service 接口
 * 
 * @author zjh - kayson
 */
public interface AdminUserService {

    /**
     * 创建用户
     *
     * @param reqVO 用户信息
     * @return 用户编号
     */
    Long createUser(@Valid UserCreateReqVO reqVO);


    /**
     * 修改用户
     *
     * @param reqVO 用户信息
     */
    void updateUser(@Valid UserUpdateReqVO reqVO);

    /**
     * 删除用户
     *
     * @param id 用户编号
     */
    void deleteUser(Long id);

    /**
     * 通过用户 ID 查询用户
     *
     * @param id 用户ID
     * @return 用户对象信息
     */
    AdminUserDO getUser(Long id);

    /**
     * 获得用户分页列表
     *
     * @param reqVO 分页条件
     * @return 分页列表
     */
    PageResult<AdminUserDO> getUserPage(UserPageReqVO reqVO);

    /**
     * 重置用户密码
     * 
     * @param id 用户编号
     * @param password 密码
     */
    void updateUserPassword(Long id, String password);

    /**
     * 修改用户状态
     * 
     * @param id 用户编号
     * @param status 状态
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 获得指定状态的用户们
     *
     * @param status 状态
     * @return 用户们
     */
    List<AdminUserDO> getUserListByStatus(Integer status);

    /**
     * 查询用户
     * 
     * @param username 用户名
     * @return 用户
     */
    AdminUserDO getUserByUsername(String username);

    /**
     * 判断密码是否匹配
     * 
     * @param password 未加密的密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    boolean isPasswordMatch(String password, String encodedPassword);

    /**
     * 更新用户的最后登陆信息
     *
     * @param id 用户编号
     * @param loginIp 登陆 IP
     */
    void updateUserLogin(Long id, String loginIp);

    /**
     * 修改用户个人信息
     *
     * @param id 用户编号
     * @param reqVO 用户个人信息
     */
    void updateUserProfile(Long id, UserProfileUpdateReqVO reqVO);

    /**
     * 修改用户个人密码
     *
     * @param id 用户编号
     * @param reqVO 更新用户个人密码
     */
    void updateUserPassword(Long id, @Valid UserProfileUpdatePasswordReqVO reqVO);

    /**
     * 更新用户头像
     *
     * @param id         用户 id
     * @param avatarFile 头像文件
     */
    String updateUserAvatar(Long id, InputStream avatarFile) throws Exception;

    /**
     * 获得用户列表，基于昵称模糊匹配
     *
     * @param nickname 昵称
     * @return 用户列表
     */
    List<AdminUserDO> getUserListByNickname(String nickname);

    /**
     * 获得用户 Map
     *
     * @param ids 用户编号数组
     * @return 用户 Map
     */
    default Map<Long, AdminUserDO> getUserMap(Collection<Long> ids) {
        return CollectionUtils.convertMap(getUserList(ids), AdminUserDO::getId);
    }

    /**
     * 获得用户列表
     *
     * @param ids 用户编号数组
     * @return 用户列表
     */
    List<AdminUserDO> getUserList(Collection<Long> ids);

    /**
     * 获得用户列表
     *
     * @param reqVO 列表请求
     * @return 用户列表
     */
    List<AdminUserDO> getUserList(UserExportReqVO reqVO);

    /**
     * 批量导入用户
     *
     * @param importUsers     导入用户列表
     * @param isUpdateSupport 是否支持更新
     * @return 导入结果
     */
    UserImportRespVO importUserList(List<UserImportExcelVO> importUsers, boolean isUpdateSupport);
    
}
