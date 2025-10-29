package com.springjwt.module.scheduler.domain.entity;

import com.springjwt.common.base.entity.BaseEntity;
import com.springjwt.common.enums.JobStatus;
import com.springjwt.common.util.MapToJsonConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(
    name = "scheduled_job",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_job_name_group",
            columnNames = {"job_name", "job_group"}
        )
    },
    indexes = {
        @Index(name = "idx_job_status", columnList = "status"),
        @Index(name = "idx_job_name_group", columnList = "job_name, job_group")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name", nullable = false, length = 100)
    private String jobName;

    @Column(name = "job_group", nullable = false, length = 100)
    private String jobGroup;

    @Column(name = "job_class", nullable = false)
    private String jobClass;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private JobStatus status = JobStatus.ACTIVE;

    @Column(columnDefinition = "JSON")
    @Convert(converter = MapToJsonConverter.class)
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JobExecutionHistory> executionHistories = new ArrayList<>();
}
