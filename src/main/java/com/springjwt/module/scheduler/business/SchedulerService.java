package com.springjwt.module.scheduler.business;

import com.springjwt.common.base.request.BasePagingRequest;
import com.springjwt.common.base.response.PagedResult;
import com.springjwt.module.scheduler.domain.entity.ScheduledJob;
import com.springjwt.module.scheduler.model.dto.JobExecutionDTO;
import com.springjwt.module.scheduler.model.dto.ScheduledJobDTO;
import com.springjwt.module.scheduler.model.request.CreateJobRequest;
import com.springjwt.module.scheduler.model.request.UpdateJobRequest;

import java.util.List;

public interface SchedulerService {

    ScheduledJobDTO createJob(CreateJobRequest request);

    ScheduledJobDTO updateJob(Long jobId, UpdateJobRequest request);

    void deleteJob(Long jobId);

    ScheduledJobDTO getJobById(Long jobId);

    PagedResult<ScheduledJobDTO> getAllJobs(BasePagingRequest request);

    void pauseJob(Long jobId);

    void resumeJob(Long jobId);

    void triggerJob(Long jobId);

    void scheduleJob(ScheduledJob job);

    void rescheduleJob(ScheduledJob job);

    void unscheduleJob(ScheduledJob job);

    PagedResult<JobExecutionDTO> getJobExecutionHistory(Long jobId, BasePagingRequest request);

    List<ScheduledJobDTO> getActiveJobs();
}
