package cn.zjh.kayson.module.system.dal.dataobject.dept;

import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.tenant.core.db.TenantBaseDO;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门表
 * 
 * @author zjh - kayson
 */
@TableName("system_dept")
@Data
@EqualsAndHashCode(callSuper = true)
public class DeptDO extends TenantBaseDO {

    /**
     * 部门ID
     */
    @TableId
    private Long id;
    /**
     * 部门名称
     */
    private String name;
    /**
     * 父部门ID
     *
     * 关联 {@link #id}
     */
    private Long parentId;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 负责人
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long leaderUserId;
    /**
     * 联系电话
     */
    private String phone;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 部门状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    
}
