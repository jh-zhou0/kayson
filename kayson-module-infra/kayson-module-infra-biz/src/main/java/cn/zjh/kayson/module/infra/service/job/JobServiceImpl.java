package cn.zjh.kayson.module.infra.service.job;

import cn.hutool.core.collection.CollUtil;
import cn.zjh.kayson.framework.common.pojo.PageResult;
import cn.zjh.kayson.framework.quartz.core.schedler.SchedulerManager;
import cn.zjh.kayson.framework.quartz.core.util.CronUtils;
import cn.zjh.kayson.module.infra.controller.admin.job.vo.job.JobCreateReqVO;
import cn.zjh.kayson.module.infra.controller.admin.job.vo.job.JobExportReqVO;
import cn.zjh.kayson.module.infra.controller.admin.job.vo.job.JobPageReqVO;
import cn.zjh.kayson.module.infra.controller.admin.job.vo.job.JobUpdateReqVO;
import cn.zjh.kayson.module.infra.convert.job.JobConvert;
import cn.zjh.kayson.module.infra.dal.dataobject.job.JobDO;
import cn.zjh.kayson.module.infra.dal.mysql.job.JobMapper;
import cn.zjh.kayson.module.infra.enums.job.JobStatusEnum;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static cn.zjh.kayson.framework.common.exception.util.ServiceExceptionUtils.exception;
import static cn.zjh.kayson.module.infra.enums.ErrorCodeConstants.*;

/**
 * 定时任务 Service 实现类
 * 
 * @author zjh - kayson
 */
@Service
@Validated
public class JobServiceImpl implements JobService {

    @Resource
    private JobMapper jobMapper;

    @Resource
    private SchedulerManager schedulerManager;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createJob(JobCreateReqVO createReqVO) throws SchedulerException {
        // 校验 cron 表达式
        validateCronExpression(createReqVO.getCronExpression());
        // 校验唯一
        validateJobHandlerNameUnique(createReqVO.getHandlerName());
        
        // 插入 Job
        JobDO job = JobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(JobStatusEnum.INIT.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);

        // 添加 Job 到 Quartz 中
        schedulerManager.addJob(job.getId(), job.getHandlerName(), job.getHandlerParam(), job.getCronExpression(), 
                job.getRetryCount(), job.getRetryInterval());
        // 更新 Job 状态
        JobDO updateObj = JobDO.builder().id(job.getId()).status(JobStatusEnum.NORMAL.getStatus()).build();
        jobMapper.updateById(updateObj);
        
        return job.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJob(JobUpdateReqVO updateReqVO) throws SchedulerException {
        // 校验 cron 表达式
        validateCronExpression(updateReqVO.getCronExpression());
        // 校验存在
        JobDO job = validateJobExists(updateReqVO.getId());
        // 只有开启状态，才可以修改.原因是，如果是暂停状态，修改 Quartz Job 时，会导致任务又开始执行
        if (!job.getStatus().equals(JobStatusEnum.NORMAL.getStatus())) {
            throw exception(JOB_UPDATE_ONLY_NORMAL_STATUS);
        }

        // 更新
        JobDO updateObj = JobConvert.INSTANCE.convert(updateReqVO);
        fillJobMonitorTimeoutEmpty(updateObj);
        jobMapper.updateById(updateObj);

        // 更新 Job 到 Quartz 中
        schedulerManager.updateJob(job.getHandlerName(), updateReqVO.getHandlerParam(), updateReqVO.getCronExpression(),
                updateReqVO.getRetryCount(), updateReqVO.getRetryInterval());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJobStatus(Long id, Integer status) throws SchedulerException {
        // 校验 status
        if (!CollUtil.contains(Arrays.asList(JobStatusEnum.NORMAL.getStatus(), JobStatusEnum.STOP.getStatus()), status)) {
            throw exception(JOB_CHANGE_STATUS_INVALID);
        }
        // 校验存在
        JobDO job = validateJobExists(id);
        // 校验是否已经为当前状态
        if (job.getStatus().equals(status)) {
            throw exception(JOB_CHANGE_STATUS_EQUALS);
        }

        // 更新 Job 状态
        JobDO updateObj = JobDO.builder().id(id).status(status).build();
        jobMapper.updateById(updateObj);

        // 更新状态 Job 到 Quartz 中
        if (JobStatusEnum.NORMAL.getStatus().equals(status)) { // 开启
            schedulerManager.resumeJob(job.getHandlerName());
        } else { // 暂停
            schedulerManager.pauseJob(job.getHandlerName());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(Long id) throws SchedulerException {
        // 校验存在
        JobDO job = validateJobExists(id);
        // 删除
        jobMapper.deleteById(id);
        // 从 Quartz 中 删除 Job
        schedulerManager.deleteJob(job.getHandlerName());
    }

    @Override
    public void triggerJob(Long id) throws SchedulerException {
        // 校验存在
        JobDO job = validateJobExists(id);
        // 触发 Quartz 中的 Job
        schedulerManager.triggerJob(job.getId(), job.getHandlerName(), job.getHandlerParam());
    }

    @Override
    public JobDO getJob(Long id) {
        return jobMapper.selectById(id);
    }

    @Override
    public List<JobDO> getJobList(Collection<Long> ids) {
        return jobMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<JobDO> getJobPage(JobPageReqVO pageReqVO) {
        return jobMapper.selectPage(pageReqVO);
    }

    @Override
    public List<JobDO> getJobList(JobExportReqVO exportReqVO) {
        return jobMapper.selectList(exportReqVO);
    }

    private void validateCronExpression(String cronExpression) {
        if (!CronUtils.isValid(cronExpression)) {
            throw exception(JOB_CRON_EXPRESSION_VALID);
        }
    }

    private void validateJobHandlerNameUnique(String handlerName) {
        if (jobMapper.selectByHandlerName(handlerName) != null) {
            throw exception(JOB_HANDLER_EXISTS);
        }
    }

    private static void fillJobMonitorTimeoutEmpty(JobDO job) {
        if (job.getMonitorTimeout() == null) {
            job.setMonitorTimeout(0);
        }
    }
    
    private JobDO validateJobExists(Long id) {
        JobDO job = jobMapper.selectById(id);
        if (job == null) {
            throw exception(JOB_NOT_EXISTS);
        }
        return job;
    }
    
}
