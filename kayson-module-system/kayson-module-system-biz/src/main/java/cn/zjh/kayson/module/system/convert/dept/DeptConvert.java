package cn.zjh.kayson.module.system.convert.dept;

import cn.zjh.kayson.module.system.controller.admin.dept.vo.dept.DeptCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.dept.DeptRespVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.dept.DeptUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.dept.DeptDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface DeptConvert {

    DeptConvert INSTANCE = Mappers.getMapper(DeptConvert.class);
    
    DeptDO convert(DeptCreateReqVO bean);
    
    DeptDO convert(DeptUpdateReqVO bean);
    
    DeptRespVO convert(DeptDO bean);

    List<DeptRespVO> convertList(List<DeptDO> list);
}
