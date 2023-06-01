package cn.zjh.kayson.module.infra.service.file;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.file.core.client.FileClient;
import cn.zjh.kayson.framework.test.core.ut.BaseDbUnitTest;
import cn.zjh.kayson.framework.test.core.util.AssertUtils;
import cn.zjh.kayson.module.infra.controller.admin.file.vo.file.FilePageReqVO;
import cn.zjh.kayson.module.infra.dal.dataobject.file.FileDO;
import cn.zjh.kayson.module.infra.dal.mysql.file.FileMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.zjh.kayson.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.zjh.kayson.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.zjh.kayson.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.*;
import static cn.zjh.kayson.module.infra.enums.ErrorCodeConstants.FILE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

/**
 * @author zjh - kayson
 */
@Import(FileServiceImpl.class)
public class FileServiceImplTest extends BaseDbUnitTest {
    
    @Resource
    private FileServiceImpl fileService;

    @Resource
    private FileMapper fileMapper;

    @MockBean
    private FileConfigService fileConfigService;

    @Test
    void testCreateFile_success() throws Exception {
        // 准备参数
        String name = "单测文件名";
        String path = randomString();
        byte[] content = ResourceUtil.readBytes("file/img_cat.png");
        // mock 方法
        FileClient client = mock(FileClient.class);
        when(fileConfigService.getMasterFileClient()).thenReturn(client);
        String url = randomString();
        when(client.upload(same(content), eq(path), eq("image/png"))).thenReturn(url);
        when(client.getId()).thenReturn(1L);
        
        // 调用
        String result = fileService.createFile(name, path, content);
        // 断言
        assertEquals(url, result);
        // 校验数据
        FileDO file = fileMapper.selectOne(FileDO::getPath, path);
        assertEquals(1L, file.getConfigId());
        assertEquals(path, file.getPath());
        assertEquals(url, file.getUrl());
        assertEquals("image/png", file.getType());
        assertEquals(content.length, file.getSize());
    }

    @Test
    void testDeleteFile_success() throws Exception {
        // mock 数据
        FileDO dbFile = randomPojo(FileDO.class, o -> o.setConfigId(10L).setPath("apple.jpg"));
        fileMapper.insert(dbFile);// @Sql: 先插入出一条存在的数据
        // mock Master 文件客户端
        FileClient client = mock(FileClient.class);
        when(fileConfigService.getFileClient(eq(10L))).thenReturn(client);
        // 准备参数
        Long id = dbFile.getId();

        // 调用
        fileService.deleteFile(id);
        // 校验数据不存在了
        assertNull(fileMapper.selectById(id));
        // 校验调用
        verify(client).delete(eq("apple.jpg"));
    }

    @Test
    public void testDeleteFile_notExists() {
        // 准备参数
        Long id = randomLong();

        // 调用, 并断言异常
        assertServiceException(() -> fileService.deleteFile(id), FILE_NOT_EXISTS);
    }

    @Test
    public void testGetFilePage() {
        // mock 数据
        FileDO dbFile = randomPojo(FileDO.class, o -> { // 等会查询到
            o.setPath("kayson");
            o.setType("image/jpeg");
            o.setCreateTime(buildTime(2023, 6, 1));
        });
        fileMapper.insert(dbFile);
        // 测试 path 不匹配
        fileMapper.insert(cloneIgnoreId(dbFile, o -> o.setPath("apple")));
        // 测试 type 不匹配
        fileMapper.insert(cloneIgnoreId(dbFile, o -> o.setType("image/png")));
        // 测试 createTime 不匹配
        fileMapper.insert(cloneIgnoreId(dbFile, o -> o.setCreateTime(buildTime(2023, 5, 1))));
        // 准备参数
        FilePageReqVO reqVO = new FilePageReqVO();
        reqVO.setPath("kayson");
        reqVO.setType("jpeg");
        reqVO.setCreateTime(buildBetweenTime(2023, 5, 10, 2023, 6, 2));

        // 调用
        PageResult<FileDO> pageResult = fileService.getFilePage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        AssertUtils.assertPojoEquals(dbFile, pageResult.getList().get(0));
    }

    @Test
    public void testGetFileContent() throws Exception {
        // 准备参数
        Long configId = 10L;
        String path = "apple.jpg";
        // mock 方法
        FileClient client = mock(FileClient.class);
        when(fileConfigService.getFileClient(eq(10L))).thenReturn(client);
        byte[] content = new byte[]{};
        when(client.getContent(eq("apple.jpg"))).thenReturn(content);

        // 调用
        byte[] result = fileService.getFileContent(configId, path);
        // 断言
        assertSame(result, content);
    }
    
}
