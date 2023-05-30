package cn.zjh.kayson.module.system.convert.dict;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.type.*;
import cn.zjh.kayson.module.system.dal.dataobject.dict.DictTypeDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface DictTypeConvert {
    
    DictTypeConvert INSTANCE = Mappers.getMapper(DictTypeConvert.class);
    
    DictTypeDO convert(DictTypeCreateReqVO bean);
    
    DictTypeDO convert(DictTypeUpdateReqVO bean);

    PageResult<DictTypeRespVO> convertPage(PageResult<DictTypeDO> page);

    DictTypeRespVO convert(DictTypeDO dictType);

    List<DictTypeSimpleRespVO> convertList(List<DictTypeDO> list);

    List<DictTypeExcelVO> convertList01(List<DictTypeDO> list);
}
