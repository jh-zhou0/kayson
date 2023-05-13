package cn.zjh.kayson.module.system.controller.admin.permission.vo.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zjh - kayson
 */
@Schema(description = "管理后台 - 菜单创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuCreateReqVO extends MenuBaseVO{
}
