package cn.zjh.kayson.framework.dict.config;

import cn.zjh.kayson.framework.dict.core.util.DictFrameworkUtils;
import cn.zjh.kayson.module.system.api.dict.DictDataApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author zjh - kayson
 */
@AutoConfiguration
public class KaysonDictAutoConfiguration {
    
    @Bean
    @SuppressWarnings("InstantiationOfUtilityClass")
    public DictFrameworkUtils dictFrameworkUtils(DictDataApi dictDataApi) {
        DictFrameworkUtils.init(dictDataApi);
        return new DictFrameworkUtils();
    }
    
}
