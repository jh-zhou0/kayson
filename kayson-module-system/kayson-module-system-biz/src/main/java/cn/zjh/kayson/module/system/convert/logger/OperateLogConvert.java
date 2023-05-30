package cn.zjh.kayson.module.system.convert.logger;

import cn.zjh.kayson.framework.common.util.collection.MapUtils;
import cn.zjh.kayson.module.system.api.logger.dto.OperateLogCreateReqDTO;
import cn.zjh.kayson.module.system.controller.admin.logger.vo.operatelog.OperateLogExcelVO;
import cn.zjh.kayson.module.system.controller.admin.logger.vo.operatelog.OperateLogRespVO;
import cn.zjh.kayson.module.system.dal.dataobject.logger.OperateLogDO;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.zjh.kayson.framework.common.exception.enums.GlobalErrorCodeConstants.SUCCESS;

/**
 * @author zjh - kayson
 */
@Mapper
public interface OperateLogConvert {

    OperateLogConvert INSTANCE = Mappers.getMapper(OperateLogConvert.class);
    
    OperateLogDO convert(OperateLogCreateReqDTO bean);

    OperateLogRespVO convert(OperateLogDO bean);

    default List<OperateLogExcelVO> convertList(List<OperateLogDO> list, Map<Long, AdminUserDO> userMap) {
         return list.stream().map(operateLog -> {
             OperateLogExcelVO excelVO = convert01(operateLog);
             MapUtils.findAndThen(userMap, operateLog.getUserId(), user -> excelVO.setUserNickname(user.getNickname()));
             excelVO.setSuccessStr(SUCCESS.getCode().equals(operateLog.getResultCode()) ? "成功" : "失败");
             return excelVO;
         }).collect(Collectors.toList());
    }
    
    OperateLogExcelVO convert01(OperateLogDO bean);
    
}
