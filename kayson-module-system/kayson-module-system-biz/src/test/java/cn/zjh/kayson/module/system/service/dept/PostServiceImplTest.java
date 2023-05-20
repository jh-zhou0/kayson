package cn.zjh.kayson.module.system.service.dept;

import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.common.util.object.ObjectUtils;
import cn.zjh.kayson.framework.test.core.ut.BaseDbUnitTest;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.post.PostCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.post.PostPageReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.post.PostUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.dept.PostDO;
import cn.zjh.kayson.module.system.dal.mysql.dept.PostMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.zjh.kayson.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.zjh.kayson.framework.test.core.util.RandomUtils.*;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author zjh - kayson
 */
@Import(PostServiceImpl.class)
public class PostServiceImplTest extends BaseDbUnitTest {
    
    @Resource
    private PostServiceImpl postService;
    
    @Resource
    private PostMapper postMapper;

    @Test
    public void testCreatePost_success() {
        // 准备参数
        PostCreateReqVO reqVO = randomPojo(PostCreateReqVO.class, o -> o.setStatus(randomCommonStatus()));
        // 调用
        Long postId = postService.createPost(reqVO);

        // 断言
        assertNotNull(postId);
        // 校验记录的属性是否正确
        PostDO post = postMapper.selectById(postId);
        assertPojoEquals(reqVO, post);
    }

    @Test
    void testUpdatePost_success() {
        // mock 数据
        PostDO postDO = randomPojo(PostDO.class, o -> o.setStatus(randomCommonStatus()));
        postMapper.insert(postDO);
        // 准备参数
        PostUpdateReqVO reqVO = randomPojo(PostUpdateReqVO.class, o -> 
                o.setId(postDO.getId()).setName("update").setStatus(randomCommonStatus()));
        
        // 调用
        postService.updatePost(reqVO);
        // 校验是否更新正确
        PostDO post = postMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, post);
    }

    @Test
    void testDeletePost_success() {
        // mock 数据
        PostDO postDO = randomPojo(PostDO.class, o -> o.setStatus(randomCommonStatus()));
        postMapper.insert(postDO);
        // 准备参数
        Long id = postDO.getId();

        // 调用
        postService.deletePost(id);
        assertNull(postMapper.selectById(id));
    }

    @Test
    void testValidatePost_notFoundForDelete() {
        // 准备参数
        Long id = randomLong();
        
        // 调用, 并断言异常
        assertServiceException(() -> postService.deletePost(id), POST_NOT_FOUND);
    }

    @Test
    void testValidatePost_nameDuplicateForCreate() {
        // mock 数据
        PostDO postDO = randomPojo(PostDO.class, o -> o.setStatus(randomCommonStatus()));
        postMapper.insert(postDO);
        // 准备参数
        PostCreateReqVO reqVO = randomPojo(PostCreateReqVO.class, o -> o.setName(postDO.getName()));

        // 调用, 并断言异常
        assertServiceException(() -> postService.createPost(reqVO), POST_NAME_DUPLICATE);
    }

    @Test
    void testValidatePost_codeDuplicateForUpdate() {
        // mock 数据
        PostDO postDO = randomPojo(PostDO.class, o -> o.setStatus(randomCommonStatus()));
        postMapper.insert(postDO);
        // mock 数据
        PostDO codePostDO = randomPojo(PostDO.class, o -> o.setStatus(randomCommonStatus()));
        postMapper.insert(codePostDO);
        // 准备参数
        PostUpdateReqVO reqVO = randomPojo(PostUpdateReqVO.class, o ->
                o.setId(postDO.getId()).setCode(codePostDO.getCode()));

        // 调用, 并断言异常
        assertServiceException(() -> postService.updatePost(reqVO), POST_CODE_DUPLICATE);
    }

    @Test
    void testGetPostPage() {
        // mock 数据
        PostDO postDO = randomPojo(PostDO.class, o -> o.setName("码仔").setStatus(CommonStatusEnum.ENABLE.getValue()));
        postMapper.insert(postDO);
        postMapper.insert(ObjectUtils.cloneIgnoreId(postDO, o -> o.setName("程序员")));
        postMapper.insert(ObjectUtils.cloneIgnoreId(postDO, o -> o.setStatus(CommonStatusEnum.DISABLE.getValue())));
        // 准备参数
        PostPageReqVO reqVO = new PostPageReqVO().setName("码").setStatus(CommonStatusEnum.ENABLE.getValue());

        // 调用
        PageResult<PostDO> pageResult = postService.getPostPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getList().size());
        assertEquals(1, pageResult.getTotal());
        assertPojoEquals(postDO, pageResult.getList().get(0));
    }

    @Test
    void testGetPost() {
        // mock 数据
        PostDO postDO = randomPojo(PostDO.class, o -> o.setStatus(randomCommonStatus()));
        postMapper.insert(postDO);
        // 准备参数
        Long id = postDO.getId();
        
        // 调用
        PostDO post = postService.getPost(id);
        // 断言
        assertNotNull(post);
        assertPojoEquals(postDO, post);
    }
}
