package cn.zjh.kayson.module.infra.convert.file;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.infra.controller.admin.file.vo.file.FileRespVO;
import cn.zjh.kayson.module.infra.dal.dataobject.file.FileDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author zjh - kayson
 */
@Mapper
public interface FileConvert {
    
    FileConvert INSTANCE = Mappers.getMapper(FileConvert.class);

    PageResult<FileRespVO> convertPage(PageResult<FileDO> page);
    
}
