package cn.zjh.kayson.module.infra.service.test;

import cn.zjh.kayson.module.infra.dal.dataobject.test.TestDemoDO;

import java.util.List;

/**
 * @author zjh - kayson
 */
public interface TestDemoService {

    /**
     * 使用 xml 形式，获得 TestDO 列表
     */
    List<TestDemoDO> getList();
    
}
