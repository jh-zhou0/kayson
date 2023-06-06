package cn.zjh.kayson.framework.security;

import cn.hutool.core.map.MapUtil;
import cn.zjh.kayson.framework.common.enums.UserTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 上下文，不持久化
     * 
     * 属于 LoginUser 维度的临时缓存
     */
    @JsonIgnore
    private Map<String, Object> context;

    public void setContext(String key, Object value) {
        if (context == null) {
            context = new HashMap<>();
        }
        context.put(key, value);
    }
    
    public <T> T getContext(String key, Class<T> type) {
        return MapUtil.get(context, key, type);
    }
    
}
