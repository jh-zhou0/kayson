package cn.zjh.kayson.module.system.controller.admin.user.vo.user;

import cn.zjh.kayson.framework.excel.core.annotations.DictFormat;
import cn.zjh.kayson.framework.excel.core.convert.DictConvert;
import cn.zjh.kayson.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 用户 Excel 导入 VO
 * 
 * @author zjh - kayson
 */
@Data
public class UserImportExcelVO {

    @ExcelProperty("登录名称")
    private String username;

    @ExcelProperty("用户名称")
    private String nickname;

    @ExcelProperty("部门编号")
    private Long deptId;

    @ExcelProperty("用户邮箱")
    private String email;

    @ExcelProperty("手机号码")
    private String mobile;

    @ExcelProperty(value = "用户性别", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.USER_SEX)
    private Integer sex;

    @ExcelProperty(value = "账号状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

}
