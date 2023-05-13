package cn.zjh.kayson.module.system.controller.admin.permission.vo.role;

import cn.zjh.kayson.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author zjh - kayson
 */
@Schema(description = "管理后台 - 角色分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class RolePageReqVO extends PageParam {

    @Schema(description = "角色名称,模糊匹配", example = "kayson")
    private String name;

    @Schema(description = "角色标识,模糊匹配", example = "kayson")
    private String code;

    @Schema(description = "展示状态,参见 CommonStatusEnum 枚举类", example = "1")
    private Integer status;

    @Schema(description = "创建时间", example = "2022-07-01 00:00:00,2022-07-01 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime[] createTime;

}
