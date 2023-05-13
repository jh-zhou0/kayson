package cn.zjh.kayson.module.system.controller.admin.oauth2.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author zjh - kayson
 */
@Schema(description = "管理后台 - OAuth2 客户端创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OAuth2ClientCreateReqVO extends OAuth2ClientBaseVO {
}
