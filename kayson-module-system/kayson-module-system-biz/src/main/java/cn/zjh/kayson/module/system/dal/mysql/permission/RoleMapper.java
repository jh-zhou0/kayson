package cn.zjh.kayson.module.system.dal.mysql.permission;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.mybatis.core.mapper.BaseMapperX;
import cn.zjh.kayson.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.zjh.kayson.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.permission.RoleDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zjh - kayson
 */
@Mapper
public interface RoleMapper extends BaseMapperX<RoleDO> {
    
    default RoleDO selectByName(String name) {
        return selectOne(RoleDO::getName, name);
    }

    default RoleDO selectByCode(String code) {
        return selectOne(RoleDO::getCode, code);
    }

    default PageResult<RoleDO> selectPage(RolePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RoleDO>()
                .likeIfPresent(RoleDO::getName, reqVO.getName())
                .likeIfPresent(RoleDO::getCode, reqVO.getCode())
                .eqIfPresent(RoleDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(RoleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(RoleDO::getId));
    }
}
