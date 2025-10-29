package com.springjwt.module.scheduler.domain.repository;

import com.springjwt.common.enums.JobStatus;
import com.springjwt.module.scheduler.domain.entity.ScheduledJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, Long> {

    Optional<ScheduledJob> findByJobNameAndJobGroup(String jobName, String jobGroup);

    List<ScheduledJob> findByStatus(JobStatus status);

    boolean existsByJobNameAndJobGroup(String jobName, String jobGroup);
}

