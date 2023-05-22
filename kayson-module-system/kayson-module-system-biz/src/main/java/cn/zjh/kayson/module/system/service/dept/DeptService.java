package cn.zjh.kayson.module.system.service.dept;

import cn.zjh.kayson.module.system.controller.admin.dept.vo.dept.DeptCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.dept.DeptUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.dept.DeptDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 部门 Service 接口
 * 
 * @author zjh - kayson
 */
public interface DeptService {

    /**
     * 创建部门
     *
     * @param reqVO 部门信息
     * @return 部门编号
     */
    Long createDept(DeptCreateReqVO reqVO);

    /**
     * 更新部门
     *
     * @param reqVO 部门信息
     */
    void updateDept(DeptUpdateReqVO reqVO);

    /**
     * 删除部门
     *
     * @param id 部门编号
     */
    void deleteDept(Long id);

    /**
     * 获得部门信息
     *
     * @param id 部门编号
     * @return 部门信息
     */
    DeptDO getDept(Long id);

    /**
     * 筛选部门列表
     *
     * @param reqVO 筛选条件请求 VO
     * @return 部门列表
     */
    List<DeptDO> getDeptList(DeptListReqVO reqVO);

    /**
     * 获得指定编号的部门 Map
     *
     * @param ids 部门编号数组
     * @return 部门 Map
     */
    Map<Long, DeptDO> getDeptMap(Collection<Long> ids);

    /**
     * 获取指定部门编号的子部门
     * 
     * @param id 部门编号
     * @return 子部门
     */
    List<DeptDO> getDeptChildrenById(Long id);

    /**
     * 校验部门们是否有效。如下情况，视为无效：
     * 1. 部门编号不存在
     * 2. 部门被禁用
     *
     * @param ids 角色编号数组
     */
    void validateDeptList(Collection<Long> ids);
}
