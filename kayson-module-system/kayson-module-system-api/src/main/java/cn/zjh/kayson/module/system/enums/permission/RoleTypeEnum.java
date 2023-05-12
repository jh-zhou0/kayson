package cn.zjh.kayson.module.system.enums.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zjh - kayson
 */
@Getter
@AllArgsConstructor
public enum RoleTypeEnum {

    /**
     * 内置角色
     */
    SYSTEM(1),
    /**
     * 自定义角色
     */
    CUSTOM(2);

    private final Integer type;
    
}
