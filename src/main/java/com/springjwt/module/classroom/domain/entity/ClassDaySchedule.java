package com.springjwt.module.classroom.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "class_day_schedules", indexes = {
        @Index(name = "idx_class_day_schedules_class", columnList = "class_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_class_day_schedules_position", columnNames = {"class_id", "position"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassDaySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "day", nullable = false, length = 3)
    private String day;

    // Stored as "HH:mm" strings to mirror the FE form representation exactly,
    // avoiding off-by-one timezone surprises that LocalTime would invite when
    // round-tripping through JSON.
    @Column(name = "start_time", nullable = false, length = 5)
    private String startTime;

    @Column(name = "end_time", nullable = false, length = 5)
    private String endTime;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
