package cn.zjh.kayson.module.infra.dal.mysql.test;

import cn.zjh.kayson.framework.mybatis.core.mapper.BaseMapperX;
import cn.zjh.kayson.module.infra.dal.dataobject.test.TestDemoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface TestDemoMapper extends BaseMapperX<TestDemoDO> {

    List<TestDemoDO> selectList2();
    
}
