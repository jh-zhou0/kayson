package cn.zjh.kayson.framework.security;

import cn.zjh.kayson.framework.common.enums.UserTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * 登录用户信息
 * 
 * @author zjh - kayson
 */
@Data
public class LoginUser {

    /**
     * 用户编号
     */
    private Long id;
    /**
     * 用户类型
     *
     * 关联 {@link UserTypeEnum}
     */
    private Integer userType;
    /**
     * 授权范围
     */
    private List<String> scopes;
    
}
