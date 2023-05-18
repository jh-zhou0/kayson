package cn.zjh.kayson.module.infra.convert.logger;

import cn.zjh.kayson.module.infra.api.logger.dto.ApiAccessLogCreateReqDTO;
import cn.zjh.kayson.module.infra.dal.dataobject.logger.ApiAccessLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author zjh - kayson
 */
@Mapper
public interface ApiAccessLogConvert {
    
    ApiAccessLogConvert INSTANCE = Mappers.getMapper(ApiAccessLogConvert.class);
    
    ApiAccessLogDO convert(ApiAccessLogCreateReqDTO bean);
    
}
