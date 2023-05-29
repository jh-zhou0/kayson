package cn.zjh.kayson.module.system.api.dict;

import cn.zjh.kayson.module.system.api.dict.vo.DictDataRespDTO;
import cn.zjh.kayson.module.system.convert.dict.DictDataConvert;
import cn.zjh.kayson.module.system.dal.dataobject.dict.DictDataDO;
import cn.zjh.kayson.module.system.service.dict.DictDataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zjh - kayson
 */
@Service
public class DictDataApiImpl implements DictDataApi {
    
    @Resource
    private DictDataService dictDataService;
    
    @Override
    public DictDataRespDTO getDictData(String type, String value) {
        DictDataDO dictData = dictDataService.getDictData(type, value);
        return DictDataConvert.INSTANCE.convert01(dictData);
    }

    @Override
    public DictDataRespDTO parseDictData(String type, String label) {
        DictDataDO dictData = dictDataService.parseDictData(type, label);
        return DictDataConvert.INSTANCE.convert01(dictData);
    }
}
