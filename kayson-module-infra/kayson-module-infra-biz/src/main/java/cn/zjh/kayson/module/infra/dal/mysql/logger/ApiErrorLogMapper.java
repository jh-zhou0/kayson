package cn.zjh.kayson.module.infra.dal.mysql.logger;

import cn.zjh.kayson.framework.mybatis.core.mapper.BaseMapperX;
import cn.zjh.kayson.module.infra.dal.dataobject.logger.ApiErrorLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zjh - kayson
 */
@Mapper
public interface ApiErrorLogMapper extends BaseMapperX<ApiErrorLogDO> {
}
