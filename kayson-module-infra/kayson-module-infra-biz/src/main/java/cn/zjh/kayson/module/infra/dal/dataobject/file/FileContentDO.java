package cn.zjh.kayson.module.infra.dal.dataobject.file;

import cn.zjh.kayson.framework.file.core.client.db.DBFileClient;
import cn.zjh.kayson.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 文件内容表
 * 专门用于存储 {@link DBFileClient} 的文件内容
 * 
 * @author zjh - kayson
 */
@TableName("infra_file_content")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileContentDO extends BaseDO {

    /**
     * 编号，数据库自增
     */
    @TableId
    private String id;
    /**
     * 配置编号
     *
     * 关联 {@link FileConfigDO#getId()}
     */
    private Long configId;
    /**
     * 路径，即文件名
     */
    private String path;
    /**
     * 文件内容
     */
    private byte[] content;

}

