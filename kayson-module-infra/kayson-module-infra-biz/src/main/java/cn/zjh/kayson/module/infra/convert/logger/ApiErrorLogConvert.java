package cn.zjh.kayson.module.infra.convert.logger;

import cn.zjh.kayson.module.infra.api.logger.dto.ApiErrorLogCreateReqDTO;
import cn.zjh.kayson.module.infra.dal.dataobject.logger.ApiErrorLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author zjh - kayson
 */
@Mapper
public interface ApiErrorLogConvert {

    ApiErrorLogConvert INSTANCE = Mappers.getMapper(ApiErrorLogConvert.class);
    
    ApiErrorLogDO convert(ApiErrorLogCreateReqDTO bean);
            
}
