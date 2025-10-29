package com.springjwt.module.scheduler.domain.repository;


import com.springjwt.common.enums.ExecutionStatus;
import com.springjwt.module.scheduler.domain.entity.JobExecutionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface JobExecutionHistoryRepository extends JpaRepository<JobExecutionHistory, Long> {

    /**
     * Find execution history by job ID with pagination.
     * Uses EntityGraph to eagerly fetch the job relationship and avoid N+1 query problem.
     */
    @EntityGraph(attributePaths = {"job"})
    Page<JobExecutionHistory> findByJobId(Long jobId, Pageable pageable);

    /**
     * Find execution history by job ID and status.
     * Uses EntityGraph to eagerly fetch the job relationship and avoid N+1 query problem.
     */
    @EntityGraph(attributePaths = {"job"})
    List<JobExecutionHistory> findByJobIdAndStatus(Long jobId, ExecutionStatus status);

    @Modifying
    @Query("DELETE FROM JobExecutionHistory j WHERE j.executionTime < :cutoffDate")
    int deleteByExecutionTimeBefore(@Param("cutoffDate") Instant cutoffDate);

    /**
     * Find latest executions for a job.
     * Uses JOIN FETCH to eagerly load the job relationship and avoid N+1 query problem.
     */
    @Query("SELECT j FROM JobExecutionHistory j JOIN FETCH j.job WHERE j.job.id = :jobId ORDER BY j.executionTime DESC")
    List<JobExecutionHistory> findLatestExecutions(@Param("jobId") Long jobId, Pageable pageable);
}