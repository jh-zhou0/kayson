package cn.zjh.kayson.module.system.enums;

import cn.zjh.kayson.framework.common.exception.ErrorCode;

/**
 * System 错误码枚举类
 * system 系统，使用 1-002-000-000 段
 * 
 * @author zjh - kayson
 */
public interface ErrorCodeConstants {

    // ========== AUTH 模块 1002000000 ==========
    ErrorCode AUTH_LOGIN_BAD_CREDENTIALS = new ErrorCode(1002000000, "登录失败，账号密码不正确");
    ErrorCode AUTH_LOGIN_USER_DISABLED = new ErrorCode(1002000001, "登录失败，账号被禁用");
    ErrorCode AUTH_LOGIN_CAPTCHA_CODE_ERROR = new ErrorCode(1002000004, "验证码不正确，原因：{}");
    ErrorCode AUTH_THIRD_LOGIN_NOT_BIND = new ErrorCode(1002000005, "未绑定账号，需要进行绑定");
    ErrorCode AUTH_TOKEN_EXPIRED = new ErrorCode(1002000006, "Token 已经过期");
    ErrorCode AUTH_MOBILE_NOT_EXISTS = new ErrorCode(1002000007, "手机号不存在");

    // ========== 菜单模块 1002001000 ==========
    ErrorCode MENU_NAME_DUPLICATE = new ErrorCode(1002001000, "已经存在该名字的菜单");
    ErrorCode MENU_PARENT_NOT_EXISTS = new ErrorCode(1002001001, "父菜单不存在");
    ErrorCode MENU_PARENT_ERROR = new ErrorCode(1002001002, "不能设置自己为父菜单");
    ErrorCode MENU_NOT_EXISTS = new ErrorCode(1002001003, "菜单不存在");
    ErrorCode MENU_EXISTS_CHILDREN = new ErrorCode(1002001004, "存在子菜单，无法删除");
    ErrorCode MENU_PARENT_NOT_DIR_OR_MENU = new ErrorCode(1002001005, "父菜单的类型必须是目录或者菜单");

    // ========== 角色模块 1002002000 ==========
    ErrorCode ROLE_NOT_EXISTS = new ErrorCode(1002002000, "角色不存在");
    ErrorCode ROLE_NAME_DUPLICATE = new ErrorCode(1002002001, "已经存在名为【{}】的角色");
    ErrorCode ROLE_CODE_DUPLICATE = new ErrorCode(1002002002, "已经存在编码为【{}】的角色");
    ErrorCode ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE = new ErrorCode(1002002003, "不能操作类型为系统内置的角色");
    ErrorCode ROLE_IS_DISABLE = new ErrorCode(1002002004, "名字为【{}】的角色已被禁用");
    ErrorCode ROLE_ADMIN_CODE_ERROR = new ErrorCode(1002002005, "编码【{}】不能使用");

    // ========== 用户模块 1002003000 ==========
    ErrorCode USER_USERNAME_EXISTS = new ErrorCode(1002003000, "用户账号已经存在");
    ErrorCode USER_MOBILE_EXISTS = new ErrorCode(1002003001, "手机号已经存在");
    ErrorCode USER_EMAIL_EXISTS = new ErrorCode(1002003002, "邮箱已经存在");
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1002003003, "用户不存在");
    ErrorCode USER_IMPORT_LIST_IS_EMPTY = new ErrorCode(1002003004, "导入用户数据不能为空！");
    ErrorCode USER_PASSWORD_FAILED = new ErrorCode(1002003005, "用户密码校验失败");
    ErrorCode USER_IS_DISABLE = new ErrorCode(1002003006, "名字为【{}】的用户已被禁用");
    ErrorCode USER_COUNT_MAX = new ErrorCode(1002003008, "创建用户失败，原因：超过租户最大租户配额({})！");

    // ========== 部门模块 1002004000 ==========
    ErrorCode DEPT_NAME_DUPLICATE = new ErrorCode(1002004000, "已经存在该名字的部门");
    ErrorCode DEPT_PARENT_NOT_EXITS = new ErrorCode(1002004001,"父级部门不存在");
    ErrorCode DEPT_NOT_FOUND = new ErrorCode(1002004002, "当前部门不存在");
    ErrorCode DEPT_EXITS_CHILDREN = new ErrorCode(1002004003, "存在子部门，无法删除");
    ErrorCode DEPT_PARENT_ERROR = new ErrorCode(1002004004, "不能设置自己为父部门");
    ErrorCode DEPT_EXISTS_USER = new ErrorCode(1002004005, "部门中存在员工，无法删除");
    ErrorCode DEPT_NOT_ENABLE = new ErrorCode(1002004006, "部门({})不处于开启状态，不允许选择");
    ErrorCode DEPT_PARENT_IS_CHILD = new ErrorCode(1002004007, "不能设置自己的子部门为父部门");

    // ========== 岗位模块 1002005000 ==========
    ErrorCode POST_NOT_FOUND = new ErrorCode(1002005000, "当前岗位不存在");
    ErrorCode POST_NOT_ENABLE = new ErrorCode(1002005001, "岗位({}) 不处于开启状态，不允许选择");
    ErrorCode POST_NAME_DUPLICATE = new ErrorCode(1002005002, "已经存在该名字的岗位");
    ErrorCode POST_CODE_DUPLICATE = new ErrorCode(1002005003, "已经存在该标识的岗位");

    // ========== OAuth2 客户端 1002020000 =========
    ErrorCode OAUTH2_CLIENT_NOT_EXISTS = new ErrorCode(1002020000, "OAuth2 客户端不存在");
    ErrorCode OAUTH2_CLIENT_EXISTS = new ErrorCode(1002020001, "OAuth2 客户端编号已存在");
    ErrorCode OAUTH2_CLIENT_DISABLE = new ErrorCode(1002020002, "OAuth2 客户端已禁用");
    ErrorCode OAUTH2_CLIENT_AUTHORIZED_GRANT_TYPE_NOT_EXISTS = new ErrorCode(1002020003, "不支持该授权类型");
    ErrorCode OAUTH2_CLIENT_SCOPE_OVER = new ErrorCode(1002020004, "授权范围过大");
    ErrorCode OAUTH2_CLIENT_REDIRECT_URI_NOT_MATCH = new ErrorCode(1002020005, "无效 redirect_uri: {}");
    ErrorCode OAUTH2_CLIENT_CLIENT_SECRET_ERROR = new ErrorCode(1002020006, "无效 client_secret: {}");
}
