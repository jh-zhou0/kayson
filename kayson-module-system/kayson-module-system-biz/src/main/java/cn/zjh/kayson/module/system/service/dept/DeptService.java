package cn.zjh.kayson.module.system.service.dept;

import cn.zjh.kayson.module.system.controller.admin.dept.vo.DeptCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.DeptListReqVO;
import cn.zjh.kayson.module.system.controller.admin.dept.vo.DeptUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.dept.DeptDO;

import java.util.List;

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
}
