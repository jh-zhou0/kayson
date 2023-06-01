package cn.zjh.kayson.module.infra.convert.file;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author zjh - kayson
 */
@Mapper
public interface FileConvert {
    
    FileConvert INSTANCE = Mappers.getMapper(FileConvert.class);
    
}
