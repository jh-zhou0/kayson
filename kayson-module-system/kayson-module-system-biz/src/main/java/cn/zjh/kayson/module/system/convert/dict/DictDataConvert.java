package cn.zjh.kayson.module.system.convert.dict;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.api.dict.vo.DictDataRespDTO;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.DictDataCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.DictDataRespVO;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.DictDataSimpleRespVO;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.DictDataUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.dict.DictDataDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface DictDataConvert {
    
    DictDataConvert INSTANCE = Mappers.getMapper(DictDataConvert.class);
    
    DictDataDO convert(DictDataCreateReqVO bean);
    
    DictDataDO convert(DictDataUpdateReqVO bean);

    List<DictDataSimpleRespVO> convertList(List<DictDataDO> list);

    PageResult<DictDataRespVO> convertPage(PageResult<DictDataDO> page);
    
    DictDataRespVO convert(DictDataDO bean);
    
    DictDataRespDTO convert01(DictDataDO bean);
}
