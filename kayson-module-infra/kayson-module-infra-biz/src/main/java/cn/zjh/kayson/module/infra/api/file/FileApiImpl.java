package cn.zjh.kayson.module.infra.api.file;

import cn.zjh.kayson.module.infra.api.FileApi;
import cn.zjh.kayson.module.infra.service.file.FileService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 文件 API 实现类
 * 
 * @author zjh - kayson
 */
@Service
public class FileApiImpl implements FileApi {
    
    @Resource
    private FileService fileService;
    
    @Override
    public String createFile(String name, String path, byte[] content) {
        return fileService.createFile(name, path, content);
    }
    
}
