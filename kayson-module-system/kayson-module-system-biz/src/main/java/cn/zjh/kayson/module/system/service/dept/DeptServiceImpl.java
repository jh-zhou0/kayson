package cn.zjh.kayson.module.system.service.dept;

import cn.hutool.core.collection.CollUtil;
import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.DeptCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.DeptListReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.DeptUpdateReqVO;
import cn.zjh.kayson.module.system.convert.dept.DeptConvert;
import cn.zjh.kayson.module.system.dal.dataobject.dept.DeptDO;
import cn.zjh.kayson.module.system.dal.mysql.dept.DeptMapper;
import cn.zjh.kayson.module.system.enums.dept.DeptIdEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cn.zjh.kayson.framework.common.exception.util.ServiceExceptionUtils.exception;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;

/**
 * 部门 Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
public class DeptServiceImpl implements DeptService{
    
    // 递归次数
    private static final Integer RECURSIVE_COUNT = 99;
    
    @Resource
    private DeptMapper deptMapper;
    
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
        if (CommonStatusEnum.DISABLE.getValue().equals(dept.getStatus())) {
            throw exception(DEPT_NOT_ENABLE);
        }
        // 父部门不能是原来的子部门
        List<DeptDO> children = getDeptChildrenById(id);
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

    private List<DeptDO> getDeptChildrenById(Long id) {
        if (id == null) {
            return Collections.emptyList();
        }
        List<DeptDO> result = new ArrayList<>();
        List<DeptDO> deptDOList = deptMapper.selectList();
        // 递归获取
        getDeptChildrenById(result, id, deptDOList, RECURSIVE_COUNT);
        return result;
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
