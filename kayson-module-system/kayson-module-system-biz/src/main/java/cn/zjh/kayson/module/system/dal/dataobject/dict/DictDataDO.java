package cn.zjh.kayson.module.system.dal.dataobject.dict;

import cn.zjh.kayson.framework.common.enums.CommonStatusEnum;
import cn.zjh.kayson.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据表
 * 
 * @author zjh - kayson
 */
@TableName("system_dict_data")
@Data
@EqualsAndHashCode(callSuper = true)
public class DictDataDO extends BaseDO {

    /**
     * 字典数据编号
     */
    @TableId
    private Long id;
    /**
     * 字典排序
     */
    private Integer sort;
    /**
     * 字典标签
     */
    private String label;
    /**
     * 字典值
     */
    private String value;
    /**
     * 字典类型
     *
     * 冗余 {@link DictDataDO#getDictType()}
     */
    private String dictType;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 颜色类型
     *
     * 对应到 element-ui 为 default、primary、success、info、warning、danger
     */
    private String colorType;
    /**
     * css 样式
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String cssClass;
    /**
     * 备注
     */
    private String remark;

}

