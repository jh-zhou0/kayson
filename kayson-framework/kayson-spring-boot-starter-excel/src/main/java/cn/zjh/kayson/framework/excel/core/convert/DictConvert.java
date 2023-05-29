package cn.zjh.kayson.framework.excel.core.convert;

import cn.hutool.core.convert.Convert;
import cn.zjh.kayson.framework.dict.core.util.DictFrameworkUtils;
import cn.zjh.kayson.framework.excel.core.annotations.DictFormat;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import lombok.extern.slf4j.Slf4j;

/**
 * Excel 数据字典转换器
 * 
 * @author zjh - kayson
 */
@Slf4j
public class DictConvert implements Converter<Object> {

    @Override
    public Class<?> supportJavaTypeKey() {
        throw new UnsupportedOperationException("暂不支持，也不需要");
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        throw new UnsupportedOperationException("暂不支持，也不需要");
    }

    @Override
    public Object convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, 
                                    GlobalConfiguration globalConfiguration) {
        // 使用字典解析
        String type = getType(contentProperty);
        String label = cellData.getStringValue();
        String value = DictFrameworkUtils.parseDictDataValue(type, label);
        if (value == null) {
            log.error("[convertToJavaData][type({}) 解析不掉 label({})]", type, label);
        }
        // 将 String 的 value 转换成对应的属性
        Class<?> fieldClazz = contentProperty.getField().getClass();
        return Convert.convert(fieldClazz, value);
    }

    @Override
    public WriteCellData<?> convertToExcelData(Object object, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        // 空时，返回空
        if (object == null) {
            return new WriteCellData<>("");
        }
        // 使用字典格式化
        String type = getType(contentProperty);
        String value = String.valueOf(object);
        String label = DictFrameworkUtils.getDictDataLabel(type, value);
        if (label == null) {
            log.error("[convertToExcelData][type({}) 转换不了 label({})]", type, value);
            return new WriteCellData<>("");
        }

        // 生成 Excel 小表格
        return new WriteCellData<>(label);
    }
    
    private static String getType(ExcelContentProperty contentProperty) {
        return contentProperty.getField().getAnnotation(DictFormat.class).value();
    }
    
}
