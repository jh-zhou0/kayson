package cn.zjh.kayson.module.system.service.dict;

import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.DictDataCreateReqVO;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.DictDataExportReqVO;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.DictDataPageReqVO;
import cn.zjh.kayson.module.system.controller.admin.dict.vo.data.DictDataUpdateReqVO;
import cn.zjh.kayson.module.system.dal.dataobject.dict.DictDataDO;

import java.util.List;

/**
 * 字典数据 Service 接口
 * 
 * @author zjh - kayson
 */
public interface DictDataService {

    /**
     * 创建字典数据
     *
     * @param reqVO 字典数据信息
     * @return 字典数据编号
     */
    Long createDictData(DictDataCreateReqVO reqVO);

    /**
     * 更新字典数据
     *
     * @param reqVO 字典数据信息
     */
    void updateDictData(DictDataUpdateReqVO reqVO);

    /**
     * 删除字典数据
     *
     * @param id 字典数据编号
     */
    void deleteDictData(Long id);

    /**
     * 获得字典数据列表
     *
     * @return 字典数据全列表
     */
    List<DictDataDO> getDictDataList();

    /**
     * 获得字典数据分页列表
     *
     * @param reqVO 分页请求
     * @return 字典数据分页列表
     */
    PageResult<DictDataDO> getDictDataPage(DictDataPageReqVO reqVO);
    
    /**
     * 获得字典数据详情
     *
     * @param id 字典数据编号
     * @return 字典数据
     */
    DictDataDO getDictData(Long id);

    /**
     * 获得指定字典类型的数据数量
     *
     * @param dictType 字典类型
     * @return 数据数量
     */
    long countByDictType(String dictType);

    /**
     * 获得指定的字典数据
     *
     * @param dictType 字典类型
     * @param value 字典数据值
     * @return 字典数据
     */
    DictDataDO getDictData(String dictType, String value);

    /**
     * 解析获得指定的字典数据
     *
     * @param dictType 字典类型
     * @param label 字典数据标签
     * @return 字典数据
     */
    DictDataDO parseDictData(String dictType, String label);

    /**
     * 获得字典数据列表
     *
     * @param reqVO 列表请求
     * @return 字典数据列表
     */
    List<DictDataDO> getDictDataList(DictDataExportReqVO reqVO);
    
}
