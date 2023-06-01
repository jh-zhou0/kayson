package cn.zjh.kayson.module.infra.convert.file;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.infra.controller.admin.file.vo.config.FileConfigCreateReqVO;
import cn.zjh.kayson.module.infra.controller.admin.file.vo.config.FileConfigRespVO;
import cn.zjh.kayson.module.infra.controller.admin.file.vo.config.FileConfigUpdateReqVO;
import cn.zjh.kayson.module.infra.dal.dataobject.file.FileConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @author zjh - kayson
 */
@Mapper
public interface FileConfigConvert {
    
    FileConfigConvert INSTANCE = Mappers.getMapper(FileConfigConvert.class);

    @Mapping(target = "config", ignore = true)
    FileConfigDO convert(FileConfigCreateReqVO bean);

    @Mapping(target = "config", ignore = true)
    FileConfigDO convert(FileConfigUpdateReqVO bean);

    FileConfigRespVO convert(FileConfigDO bean);

    PageResult<FileConfigRespVO> convertPage(PageResult<FileConfigDO> page);
}
