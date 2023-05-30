package cn.zjh.kayson.module.system.controller.admin.dict.vo.type;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author zjh - kayson
 */
@Schema(description = "管理后台 - 字典类型分页列表 Request VO")
@Data
public class DictTypeExportReqVO {

    @Schema(description = "字典类型名称,模糊匹配", example = "kayson")
    private String name;

    @Schema(description = "字典类型,模糊匹配", example = "sys_common_sex")
    private String type;

    @Schema(description = "展示状态,参见 CommonStatusEnum 枚举类", example = "0")
    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "[2022-07-01 00:00:00,2022-07-01 23:59:59]")
    private LocalDateTime[] createTime;

}
