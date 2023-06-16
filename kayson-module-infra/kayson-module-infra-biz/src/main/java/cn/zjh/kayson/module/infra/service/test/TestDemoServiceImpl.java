package cn.zjh.kayson.module.infra.service.test;

import cn.zjh.kayson.module.infra.dal.dataobject.test.TestDemoDO;
import cn.zjh.kayson.module.infra.dal.mysql.test.TestDemoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zjh - kayson
 */
@Service
public class TestDemoServiceImpl implements TestDemoService {
    
    @Resource
    private TestDemoMapper testDemoMapper;
    
    @Override
    public List<TestDemoDO> getList() {
        return testDemoMapper.selectList2();
    }
    
}
