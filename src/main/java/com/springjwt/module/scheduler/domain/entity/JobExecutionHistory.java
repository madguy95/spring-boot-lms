package com.springjwt.module.scheduler.domain.entity;

import com.springjwt.common.enums.ExecutionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_execution_history", indexes = {
        @Index(name = "idx_execution_time", columnList = "execution_time"),
        @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobExecutionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private ScheduledJob job;

    @Column(name = "execution_time", nullable = false)
    private Instant executionTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExecutionStatus status;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}

