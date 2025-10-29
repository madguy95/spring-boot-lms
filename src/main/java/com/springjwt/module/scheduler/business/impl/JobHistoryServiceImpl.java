package com.springjwt.module.scheduler.business.impl;

import com.springjwt.common.enums.ExecutionStatus;
import com.springjwt.module.scheduler.business.JobHistoryService;
import com.springjwt.module.scheduler.domain.entity.JobExecutionHistory;
import com.springjwt.module.scheduler.domain.entity.ScheduledJob;
import com.springjwt.module.scheduler.domain.repository.JobExecutionHistoryRepository;
import com.springjwt.module.scheduler.domain.repository.ScheduledJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobHistoryServiceImpl implements JobHistoryService {

    private final JobExecutionHistoryRepository historyRepository;
    private final ScheduledJobRepository jobRepository;

    /**
     * Save job execution history in a new independent transaction.
     * <p>
     * Uses REQUIRES_NEW to ensure:
     * 1. History is saved even if job execution fails
     * 2. History save failure doesn't affect job execution
     * 3. Each history record is committed independently
     * <p>
     * This is critical for job monitoring and debugging.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveExecutionHistory(Long jobId, ExecutionStatus status,
                                     long durationMs, String result, String errorMessage) {
        try {
            ScheduledJob job = jobRepository.findById(jobId).orElse(null);
            if (job == null) {
                log.warn("Job not found for id: {}, cannot save execution history", jobId);
                return;
            }

            JobExecutionHistory history = JobExecutionHistory.builder()
                    .job(job)
                    .executionTime(Instant.now())
                    .status(status)
                    .durationMs(durationMs)
                    .result(result)
                    .errorMessage(errorMessage)
                    .build();

            historyRepository.save(history);
            log.debug("Saved execution history for job: {}, status: {}", jobId, status);

        } catch (Exception e) {
            log.error("Failed to save execution history for job: {}", jobId, e);
            // Don't throw exception - we don't want to fail the job just because history save failed
        }
    }
}
