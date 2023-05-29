package cn.zjh.kayson.module.system.convert.dept;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.post.*;
import cn.zjh.kayson.module.system.dal.dataobject.dept.PostDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface PostConvert {
    
    PostConvert INSTANCE = Mappers.getMapper(PostConvert.class);
    
    PostDO convert(PostCreateReqVO bean);
    
    PostDO convert(PostUpdateReqVO bean);

    PostRespVO convert(PostDO bean);

    PageResult<PostRespVO> convertPage(PageResult<PostDO> page);

    List<PostSimpleRespVO> convertList(List<PostDO> list);
    
    List<PostExcelVO> convertList01(List<PostDO> list);

}
