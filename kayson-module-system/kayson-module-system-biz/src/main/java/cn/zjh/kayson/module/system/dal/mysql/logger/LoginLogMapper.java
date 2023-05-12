package cn.zjh.kayson.module.system.dal.mysql.logger;

import cn.zjh.kayson.framework.mybatis.core.mapper.BaseMapperX;
import cn.zjh.kayson.module.system.dal.dataobject.logger.LoginLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zjh - kayson
 */
@Mapper
public interface LoginLogMapper extends BaseMapperX<LoginLogDO> {
}
