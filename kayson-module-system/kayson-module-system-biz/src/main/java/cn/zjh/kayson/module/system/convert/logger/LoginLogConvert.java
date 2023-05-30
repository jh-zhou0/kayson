package cn.zjh.kayson.module.system.convert.logger;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.api.logger.dto.LoginLogCreateReqDTO;
import cn.zjh.kayson.module.system.controller.admin.logger.vo.loginlog.LoginLogExcelVO;
import cn.zjh.kayson.module.system.controller.admin.logger.vo.loginlog.LoginLogRespVO;
import cn.zjh.kayson.module.system.dal.dataobject.logger.LoginLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface LoginLogConvert {

    LoginLogConvert INSTANCE = Mappers.getMapper(LoginLogConvert.class);

    PageResult<LoginLogRespVO> convertPage(PageResult<LoginLogDO> page);
    
    LoginLogDO convert(LoginLogCreateReqDTO bean);

    List<LoginLogExcelVO> convertList(List<LoginLogDO> list);
}
