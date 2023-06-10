package cn.zjh.kayson.module.system.controller.admin.tenant.vo.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.zjh.kayson.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 租户 Excel 导出 Request VO,参数和 TenantPageReqVO 是一致的")
@Data
public class TenantExportReqVO {

    @Schema(description = "租户名", example = "kayson")
    private String name;

    @Schema(description = "联系人", example = "zjh")
    private String contactName;

    @Schema(description = "联系手机", example = "18988998899")
    private String contactMobile;

    @Schema(description = "租户状态（0正常 1停用）", example = "0")
    private Integer status;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
