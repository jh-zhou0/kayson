package cn.zjh.kayson.module.system.service.dict;

import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.DictDataCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.DictDataPageReqVO;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.DictDataUpdateReqVO;
import cn.zjh.kayson.module.system.convert.dict.DictDataConvert;
import cn.zjh.kayson.module.system.dal.dataobject.dict.DictDataDO;
import cn.zjh.kayson.module.system.dal.dataobject.dict.DictTypeDO;
import cn.zjh.kayson.module.system.dal.mysql.dict.DictDataMapper;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

import static cn.zjh.kayson.framework.common.exception.util.ServiceExceptionUtils.exception;
import static cn.zjh.kayson.module.system.enums.ErrorCodeConstants.*;

/**
 * 字典数据 Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
public class DictDataServiceImpl implements DictDataService {

    /**
     * 排序 dictType > sort
     */
    public static final Comparator<DictDataDO> COMPARATOR_TYPE_AND_SORT = Comparator
            .comparing(DictDataDO::getDictType)
            .thenComparingInt(DictDataDO::getSort);
    
    @Resource
    private DictDataMapper dictDataMapper;
    
    @Resource
    private DictTypeService dictTypeService;
    
    @Override
    public Long createDictData(DictDataCreateReqVO reqVO) {
        // 校验正确性
        validateDictDataForCreateOrUpdate(null, reqVO.getValue(), reqVO.getDictType());
        // 插入字典类型
        DictDataDO dictData = DictDataConvert.INSTANCE.convert(reqVO);
        dictDataMapper.insert(dictData);
        return dictData.getId();
    }

    @Override
    public void updateDictData(DictDataUpdateReqVO reqVO) {
        // 校验正确性
        validateDictDataForCreateOrUpdate(reqVO.getId(), reqVO.getValue(), reqVO.getDictType());
        // 更新字典类型
        DictDataDO updateObj = DictDataConvert.INSTANCE.convert(reqVO);
        dictDataMapper.updateById(updateObj);
    }

    @Override
    public void deleteDictData(Long id) {
        // 校验是否存在
        validateDictDataExists(id);
        // 删除字典类型
        dictDataMapper.deleteById(id);
    }

    @Override
    public List<DictDataDO> getDictDataList() {
        List<DictDataDO> dictDataDOList = dictDataMapper.selectList();
        dictDataDOList.sort(COMPARATOR_TYPE_AND_SORT);
        return dictDataDOList;
    }

    @Override
    public PageResult<DictDataDO> getDictDataPage(DictDataPageReqVO reqVO) {
        return dictDataMapper.selectPage(reqVO);
    }

    @Override
    public DictDataDO getDictData(Long id) {
        return dictDataMapper.selectById(id);
    }

    @Override
    public long countByDictType(String dictType) {
        return dictDataMapper.selectCountByDictType(dictType);
    }

    @Override
    public DictDataDO getDictData(String dictType, String value) {
        return dictDataMapper.selectByDictTypeAndValue(dictType, value);
    }

    @Override
    public DictDataDO parseDictData(String dictType, String label) {
        return dictDataMapper.selectByDictTypeAndLabel(dictType, label);
    }

    @VisibleForTesting
    void validateDictDataForCreateOrUpdate(Long id, String value, String dictType) {
        // 校验自己存在
        validateDictDataExists(id);
        // 校验字典类型有效
        validateDictTypeExists(dictType);
        // 校验字典数据的值的唯一性
        validateDictDataValueUnique(id, dictType, value);
    }

    @VisibleForTesting
    void validateDictDataExists(Long id) {
        if (id == null) {
            return;
        }
        DictDataDO dictData = dictDataMapper.selectById(id);
        if (dictData == null) {
            throw exception(DICT_DATA_NOT_EXISTS);
        }
    }

    @VisibleForTesting
    void validateDictTypeExists(String dictType) {
        DictTypeDO dictTypeDO = dictTypeService.getDictType(dictType);
        if (dictTypeDO == null) {
            throw exception(DICT_TYPE_NOT_EXISTS);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(dictTypeDO.getStatus())) {
            throw exception(DICT_TYPE_NOT_ENABLE);
        }
    }

    @VisibleForTesting
    void validateDictDataValueUnique(Long id, String dictType, String value) {
        DictDataDO dictData = dictDataMapper.selectByDictTypeAndValue(dictType, value);
        if (dictData == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典数据
        if (id == null) {
            throw exception(DICT_DATA_VALUE_DUPLICATE);
        }
        if (!dictData.getId().equals(id)) {
            throw exception(DICT_DATA_VALUE_DUPLICATE);
        }
    }

}
