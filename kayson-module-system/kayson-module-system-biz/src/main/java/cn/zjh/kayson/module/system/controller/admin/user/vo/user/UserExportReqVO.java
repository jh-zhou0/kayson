package cn.zjh.kayson.module.system.controller.admin.user.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.zjh.kayson.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author zjh - kayson
 */
@Schema(description = "管理后台 - 用户导出 Request VO,参数和 UserPageReqVO 是一致的")
@Data
public class UserExportReqVO {

    @Schema(description = "用户账号,模糊匹配", example = "kayson")
    private String username;

    @Schema(description = "手机号码,模糊匹配", example = "kayson")
    private String mobile;

    @Schema(description = "展示状态,参见 CommonStatusEnum 枚举类", example = "0")
    private Integer status;

    @Schema(description = "创建时间", example = "[2022-07-01 00:00:00,2022-07-01 23:59:59]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "部门编号,同时筛选子部门", example = "1024")
    private Long deptId;

}
