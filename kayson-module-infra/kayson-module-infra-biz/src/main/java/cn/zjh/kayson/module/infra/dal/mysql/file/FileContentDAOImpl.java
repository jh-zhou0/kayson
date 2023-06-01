package cn.zjh.kayson.module.infra.dal.mysql.file;

import cn.zjh.kayson.framework.file.core.client.db.DBFileContentFrameworkDAO;
import org.springframework.stereotype.Repository;

/**
 * @author zjh - kayson
 */
@Repository
public class FileContentDAOImpl implements DBFileContentFrameworkDAO {

    @Override
    public void insert(Long configId, String path, byte[] content) {
        
    }

    @Override
    public void delete(Long configId, String path) {

    }

    @Override
    public byte[] selectContent(Long configId, String path) {
        return new byte[0];
    }
    
}
