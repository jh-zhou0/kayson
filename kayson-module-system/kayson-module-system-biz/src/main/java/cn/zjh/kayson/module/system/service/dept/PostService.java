package cn.zjh.kayson.module.system.service.dept;

import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.post.PostCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.post.PostExportReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.post.PostPageReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.post.PostUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.dept.PostDO;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static cn.zjh.kayson.framework.common.util.collection.SetUtils.asSet;

/**
 * 岗位 Service 接口
 * 
 * @author zjh - kayson
 */
public interface PostService {
    
    /**
     * 创建岗位
     *
     * @param reqVO 岗位信息
     * @return 岗位编号
     */
    Long createPost(PostCreateReqVO reqVO);

    /**
     * 更新岗位
     *
     * @param reqVO 岗位信息
     */
    void updatePost(PostUpdateReqVO reqVO);

    /**
     * 删除岗位
     * 
     * @param id 岗位编号
     */
    void deletePost(Long id);

    /**
     * 获得岗位信息
     *
     * @param id 岗位编号
     * @return 岗位信息
     */
    PostDO getPost(Long id);

    /**
     * 获得岗位分页列表
     *
     * @param reqVO 分页条件
     * @return 部门分页列表
     */
    PageResult<PostDO> getPostPage(PostPageReqVO reqVO);

    /**
     * 校验岗位们是否有效。如下情况，视为无效：
     * 1. 岗位编号不存在
     * 2. 岗位被禁用
     *
     * @param ids 岗位编号数组
     */
    void validatePostList(Set<Long> ids);

    /**
     * 获得岗位列表
     *
     * @param ids 岗位编号数组。如果为空，不进行筛选
     * @return 部门列表
     */
    default List<PostDO> getPostList(@Nullable Collection<Long> ids) {
        return getPostList(ids, asSet(CommonStatusEnum.ENABLE.getStatus(), CommonStatusEnum.DISABLE.getStatus()));
    }

    /**
     * 获得符合条件的岗位列表
     *
     * @param ids 岗位编号数组。如果为空，不进行筛选
     * @param statuses 状态数组。如果为空，不进行筛选
     * @return 部门列表
     */
    List<PostDO> getPostList(@Nullable Collection<Long> ids, @Nullable Collection<Integer> statuses);

    /**
     * 获得岗位列表
     *
     * @param reqVO 查询条件
     * @return 部门列表
     */
    List<PostDO> getPostList(PostExportReqVO reqVO);
    
}
