package cn.zjh.kayson.module.system.service.logger;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.common.util.collection.CollectionUtils;
import cn.zjh.kayson.module.system.api.logger.dto.OperateLogCreateReqDTO;
import cn.zjh.kayson.module.system.controller.admin.logger.vo.operatelog.OperateLogExportReqVO;
import cn.zjh.kayson.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import cn.zjh.kayson.module.system.convert.logger.OperateLogConvert;
import cn.zjh.kayson.module.system.dal.dataobject.logger.OperateLogDO;
import cn.zjh.kayson.module.system.dal.dataobject.user.AdminUserDO;
import cn.zjh.kayson.module.system.dal.mysql.logger.OperateLogMapper;
import cn.zjh.kayson.module.system.service.user.AdminUserService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.zjh.kayson.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.zjh.kayson.module.system.dal.dataobject.logger.OperateLogDO.JAVA_METHOD_ARGS_MAX_LENGTH;
import static cn.zjh.kayson.module.system.dal.dataobject.logger.OperateLogDO.RESULT_MAX_LENGTH;

/**
 * 操作日志 Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
@Validated
public class OperateLogServiceImpl implements OperateLogService {
    
    @Resource
    private OperateLogMapper operateLogMapper;
    
    @Resource
    private AdminUserService adminUserService;
    
    @Override
    public void createOperateLog(OperateLogCreateReqDTO createReqDTO) {
        OperateLogDO logDO = OperateLogConvert.INSTANCE.convert(createReqDTO);
        logDO.setJavaMethodArgs(StrUtil.maxLength(logDO.getJavaMethodArgs(), JAVA_METHOD_ARGS_MAX_LENGTH - 3));
        logDO.setResultData(StrUtil.maxLength(logDO.getResultData(), RESULT_MAX_LENGTH - 3));
        operateLogMapper.insert(logDO);
    }

    @Override
    public PageResult<OperateLogDO> getOperateLogPage(OperateLogPageReqVO reqVO) {
        // 处理基于用户昵称的查询(OperateLogDO 中没有 nickname 字段)
        Collection<Long> userIds = null;
        if (StrUtil.isNotEmpty(reqVO.getUserNickname())) {
            List<AdminUserDO> userList = adminUserService.getUserListByNickname(reqVO.getUserNickname());
            userIds = convertSet(userList, AdminUserDO::getId);
            if (CollUtil.isEmpty(userIds)) {
                return PageResult.empty();
            }
        }
        // 查询分页
        return operateLogMapper.selectPage(reqVO, userIds);
    }

    @Override
    public List<OperateLogDO> getOperateLogList(OperateLogExportReqVO reqVO) {
        // 处理基于用户昵称的查询
        Collection<Long> userIds = null;
        if (StrUtil.isNotEmpty(reqVO.getUserNickname())) {
            userIds = convertSet(adminUserService.getUserListByNickname(reqVO.getUserNickname()), AdminUserDO::getId);
            if (CollUtil.isEmpty(userIds)) {
                return Collections.emptyList();
            }
        }
        // 查询列表
        return operateLogMapper.selectList(reqVO, userIds);
    }
}
