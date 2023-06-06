package cn.zjh.kayson.module.infra.convert.logger;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.infra.api.logger.dto.ApiAccessLogCreateReqDTO;
import cn.zjh.kayson.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogExcelVO;
import cn.zjh.kayson.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogRespVO;
import cn.zjh.kayson.module.infra.dal.dataobject.logger.ApiAccessLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface ApiAccessLogConvert {
    
    ApiAccessLogConvert INSTANCE = Mappers.getMapper(ApiAccessLogConvert.class);
    
    ApiAccessLogDO convert(ApiAccessLogCreateReqDTO bean);

    PageResult<ApiAccessLogRespVO> convertPage(PageResult<ApiAccessLogDO> page);

    List<ApiAccessLogExcelVO> convertList(List<ApiAccessLogDO> list);
    
}
