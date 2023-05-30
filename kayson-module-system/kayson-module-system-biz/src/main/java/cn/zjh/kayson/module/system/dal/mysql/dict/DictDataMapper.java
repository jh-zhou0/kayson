package cn.zjh.kayson.module.system.dal.mysql.dict;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.mybatis.core.mapper.BaseMapperX;
import cn.zjh.kayson.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.DictDataExportReqVO;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.DictDataPageReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.dict.DictDataDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.List;

/**
 * @author zjh - kayson
 */
@Mapper
public interface DictDataMapper extends BaseMapperX<DictDataDO> {

    default DictDataDO selectByDictTypeAndValue(String dictType, String value) {
        return selectOne(DictDataDO::getDictType, dictType, DictDataDO::getValue, value);
    }

    default PageResult<DictDataDO> selectPage(DictDataPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DictDataDO>()
                .likeIfPresent(DictDataDO::getLabel, reqVO.getLabel())
                .eqIfPresent(DictDataDO::getDictType, reqVO.getDictType())
                .eqIfPresent(DictDataDO::getStatus, reqVO.getStatus())
                .orderByDesc(Arrays.asList(DictDataDO::getDictType, DictDataDO::getSort)));
    }

    default long selectCountByDictType(String dictType) {
        return selectCount(DictDataDO::getDictType, dictType);
    }

    default DictDataDO selectByDictTypeAndLabel(String dictType, String label) {
        return selectOne(DictDataDO::getDictType, dictType, DictDataDO::getLabel, label);
    }

    default List<DictDataDO> selectList(DictDataExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<DictDataDO>()
                .likeIfPresent(DictDataDO::getLabel, reqVO.getLabel())
                .eqIfPresent(DictDataDO::getDictType, reqVO.getDictType())
                .eqIfPresent(DictDataDO::getStatus, reqVO.getStatus()));
    }
    
}
