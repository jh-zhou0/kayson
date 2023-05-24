package cn.zjh.kayson.module.system.service.dept;

import cn.hutool.core.collection.CollUtil;
import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.util.collection.CollectionUtils;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.dept.DeptCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.dept.DeptUpdateReqVO;
import cn.zjh.kayson.module.system.convert.dept.DeptConvert;
import cn.zjh.kayson.module.system.dal.dataobject.dept.DeptDO;
import cn.zjh.kayson.module.system.dal.mysql.dept.DeptMapper;
import cn.zjh.kayson.module.system.enums.dept.DeptIdEnum;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.zjh.kayson.framework.common.exception.util.ServiceExceptionUtils.exception;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;

/**
 * 部门 Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
@Slf4j
public class DeptServiceImpl implements DeptService{
    
    // 递归次数
    private static final Integer RECURSIVE_COUNT = 99;

    /**
     * 部门缓存
     * key：部门编号 {@link DeptDO#getId()}
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    @Getter
    private volatile Map<Long, DeptDO> deptCache;
    /**
     * 父部门缓存
     * key：部门编号 {@link DeptDO#getParentId()}
     * value: 直接子部门列表
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    @Getter
    private volatile Multimap<Long, DeptDO> parentDeptCache;
    
    @Resource
    private DeptMapper deptMapper;

    @Override
    @PostConstruct
    public void initLocalCache() {
        // 查询数据
        List<DeptDO> deptList = deptMapper.selectList();
        log.info("[initLocalCache][缓存部门，数量为:{}]", deptList.size());
        
        // 构建缓存
        ImmutableMap.Builder<Long, DeptDO> deptCacheBuilder = ImmutableMap.builder();
        ImmutableMultimap.Builder<Long, DeptDO> parentDeptCacheBuilder = ImmutableMultimap.builder();
        deptList.forEach(deptDO -> {
            deptCacheBuilder.put(deptDO.getId(), deptDO);
            parentDeptCacheBuilder.put(deptDO.getParentId(), deptDO);
        });
        deptCache = deptCacheBuilder.build();
        parentDeptCache = parentDeptCacheBuilder.build();
    }

    @Override
    public Long createDept(DeptCreateReqVO reqVO) {
        // 校验正确性
        if (reqVO.getParentId() == null) {
            reqVO.setParentId(DeptIdEnum.ROOT.getId());
        }
        validateForCreateOrUpdate(null, reqVO.getParentId(), reqVO.getName());
        // 插入部门
        DeptDO dept = DeptConvert.INSTANCE.convert(reqVO);
        deptMapper.insert(dept);
        return dept.getId();
    }

    @Override
    public void updateDept(DeptUpdateReqVO reqVO) {
        // 校验正确性
        if (reqVO.getParentId() == null) {
            reqVO.setParentId(DeptIdEnum.ROOT.getId());
        }
        validateForCreateOrUpdate(reqVO.getId(), reqVO.getParentId(), reqVO.getName());
        // 更新部门
        DeptDO updateObj = DeptConvert.INSTANCE.convert(reqVO);
        deptMapper.updateById(updateObj);
    }

    @Override
    public void deleteDept(Long id) {
        // 校验是否存在
        validateDeptExists(id);
        // 校验是否有子部门
        if (deptMapper.selectCountByParentId(id) > 0) {
            throw exception(DEPT_EXITS_CHILDREN);
        }
        // 删除部门
        deptMapper.deleteById(id);
    }

    @Override
    public DeptDO getDept(Long id) {
        return deptMapper.selectById(id);
    }

    @Override
    public List<DeptDO> getDeptList(DeptListReqVO reqVO) {
        return deptMapper.selectList(reqVO);
    }

    @Override
    public Map<Long, DeptDO> getDeptMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<DeptDO> deptDOList = deptMapper.selectBatchIds(ids);
        return CollectionUtils.convertMap(deptDOList, DeptDO::getId);
    }

    private void validateForCreateOrUpdate(Long id, Long parentId, String name) {
        // 校验自己存在
        validateDeptExists(id);
        // 校验父部门的有效性
        validateParentDeptEnable(id, parentId);
        // 校验部门名的唯一性
        validateDeptNameUnique(id, parentId, name);
    }

    private void validateDeptExists(Long id) {
        if (id == null) {
            return;
        }
        DeptDO dept = deptMapper.selectById(id);
        if (dept == null) {
            throw exception(DEPT_NOT_FOUND);
        }
    }

    private void validateParentDeptEnable(Long id, Long parentId) {
        if (parentId == null || DeptIdEnum.ROOT.getId().equals(parentId)) {
            return;
        }
        // 不能设置自己为父部门
        if (parentId.equals(id)) {
            throw exception(DEPT_PARENT_ERROR);
        }
        // 父部门不存在
        DeptDO dept = deptMapper.selectById(parentId);
        if (dept == null) {
            throw exception(DEPT_PARENT_NOT_EXITS);
        }
        // 父部门被禁用
        if (CommonStatusEnum.DISABLE.getStatus().equals(dept.getStatus())) {
            throw exception(DEPT_NOT_ENABLE);
        }
        // 父部门不能是原来的子部门
        List<DeptDO> children = getDeptListByParentIdFromCache(id, true);
        if (CollUtil.isEmpty(children)) {
            return;
        }
        if (children.stream().anyMatch(dept1 -> parentId.equals(dept1.getId()))) {
            throw exception(DEPT_PARENT_IS_CHILD);
        }
    }

    private void validateDeptNameUnique(Long id, Long parentId, String name) {
        DeptDO dept = deptMapper.selectByParentIdAndName(parentId, name);
        if (dept == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的岗位
        if (id == null) {
            throw exception(DEPT_NAME_DUPLICATE);
        }
        if (!id.equals(dept.getId())) {
            throw exception(DEPT_NAME_DUPLICATE);
        }
    }

    @Override
    public List<DeptDO> getDeptChildrenById(Long id) {
        if (id == null) {
            return Collections.emptyList();
        }
        List<DeptDO> result = new ArrayList<>();
        List<DeptDO> deptDOList = deptMapper.selectList();
        // 递归获取
        getDeptChildrenById(result, id, deptDOList, RECURSIVE_COUNT);
        return result;
    }

    @Override
    public List<DeptDO> getDeptListByParentIdFromCache(Long parentId, boolean recursive) {
        if (parentId == null) {
            return Collections.emptyList();
        }
        List<DeptDO> result = new ArrayList<>();
        getDeptListByParentIdFromCache(result, parentId, parentDeptCache, 
                recursive ? RECURSIVE_COUNT : 1); // 如果递归获取，则无限；否则，只递归 1 次
        return result;
    }

    /**
     * 递归获取所有的子部门，添加到 result 结果
     * 
     * @param result 结果
     * @param parentId 部门编号
     * @param parentDeptCache 父部门 Map，使用缓存，避免变化
     * @param recursiveCount 递归次数
     */
    private void getDeptListByParentIdFromCache(List<DeptDO> result, Long parentId, Multimap<Long, DeptDO> parentDeptCache, int recursiveCount) {
        // 终止条件
        if (recursiveCount == 0) {
            return;
        }
        // 获得子部门
        Collection<DeptDO> depts = parentDeptCache.get(parentId);
        if (CollUtil.isEmpty(depts)) {
            return;
        }
        result.addAll(depts);
        // 继续递归
        depts.forEach(dept -> getDeptListByParentIdFromCache(result, dept.getId(),
                parentDeptCache, recursiveCount - 1));
    }

    @Override
    public void validateDeptList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)){
            return;
        }
        // 获得部门信息
        Map<Long, DeptDO> deptMap = getDeptMap(ids);
        ids.forEach(id -> {
            DeptDO dept = deptMap.get(id);
            if (dept == null) {
                throw exception(DEPT_NOT_FOUND);
            }
            if (CommonStatusEnum.DISABLE.getStatus().equals(dept.getStatus())) {
                throw exception(DEPT_NOT_ENABLE);
            }
        });
    }

    private void getDeptChildrenById(List<DeptDO> result, Long id, List<DeptDO> deptDOList, Integer recursiveCount) {
        if (recursiveCount == 0) {
            return;
        }
        // 获取id部门的子部门
        List<DeptDO> children = deptDOList.stream()
                .filter(dept -> id.equals(dept.getParentId()))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(children)) {
            return;
        }
        result.addAll(children);
        children.forEach(dept -> getDeptChildrenById(result, dept.getId(), deptDOList, recursiveCount - 1));
    }
}
