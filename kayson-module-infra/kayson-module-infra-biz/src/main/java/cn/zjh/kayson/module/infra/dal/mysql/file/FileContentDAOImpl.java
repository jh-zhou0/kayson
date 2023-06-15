package cn.zjh.kayson.module.infra.dal.mysql.file;

import cn.hutool.core.collection.CollUtil;
import cn.zjh.kayson.framework.file.core.client.db.DBFileContentFrameworkDAO;
import cn.zjh.kayson.module.infra.dal.dataobject.file.FileContentDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @author zjh - kayson
 */
@Repository
public class FileContentDAOImpl implements DBFileContentFrameworkDAO {
    
    @Resource
    private FileContentMapper fileContentMapper;

    @Override
    public void insert(Long configId, String path, byte[] content) {
        FileContentDO fileContent = new FileContentDO();
        fileContent.setConfigId(configId);
        fileContent.setPath(path);
        fileContent.setContent(content);
        fileContentMapper.insert(fileContent);
    }

    @Override
    public void delete(Long configId, String path) {
        fileContentMapper.delete(buildQuery(configId, path));
    }

    @Override
    public byte[] selectContent(Long configId, String path) {
        List<FileContentDO> list = fileContentMapper.selectList(
                buildQuery(configId, path).select(FileContentDO::getContent).orderByDesc(FileContentDO::getId));
        return Optional.ofNullable(CollUtil.getFirst(list))
                .map(FileContentDO::getContent)
                .orElse(null);
    }

    private LambdaQueryWrapper<FileContentDO> buildQuery(Long configId, String path) {
        return new LambdaQueryWrapper<FileContentDO>()
                .eq(FileContentDO::getConfigId, configId)
                .eq(FileContentDO::getPath, path);
    }
    
}
