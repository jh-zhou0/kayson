package cn.zjh.kayson.module.infra.convert.logger;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.infra.api.logger.dto.ApiErrorLogCreateReqDTO;
import cn.zjh.kayson.module.infra.controller.admin.logger.vo.apierrorlog.ApiErrorLogExcelVO;
import cn.zjh.kayson.module.infra.controller.admin.logger.vo.apierrorlog.ApiErrorLogRespVO;
import cn.zjh.kayson.module.infra.dal.dataobject.logger.ApiErrorLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface ApiErrorLogConvert {

    ApiErrorLogConvert INSTANCE = Mappers.getMapper(ApiErrorLogConvert.class);
    
    ApiErrorLogDO convert(ApiErrorLogCreateReqDTO bean);

    PageResult<ApiErrorLogRespVO> convertPage(PageResult<ApiErrorLogDO> page);

    List<ApiErrorLogExcelVO> convertList(List<ApiErrorLogDO> list);
    
}
