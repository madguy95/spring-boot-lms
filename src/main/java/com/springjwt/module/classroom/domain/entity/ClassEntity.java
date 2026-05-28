package com.springjwt.module.classroom.domain.entity;

import com.springjwt.common.base.entity.BaseEntity;
import com.springjwt.module.course.domain.entity.Course;
import com.springjwt.module.teacher.domain.entity.Teacher;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

// Renamed table-binding entity ("Class" would shadow java.lang.Class so we
// use ClassEntity in code while keeping the table as `classes`).
@Entity
@Table(name = "classes", indexes = {
        @Index(name = "idx_classes_lifecycle_status", columnList = "lifecycle_status"),
        @Index(name = "idx_classes_course_id", columnList = "course_id"),
        @Index(name = "idx_classes_teacher_id", columnList = "teacher_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "label", nullable = false, length = 50)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(name = "location", nullable = false, length = 120)
    private String location;

    // Optional physical room within the campus — null when the class is online.
    // Code is sourced from master_data(type='room').
    @Column(name = "room", length = 50)
    private String room;

    // Denormalized human-readable schedule label (e.g. "Mon · Wed · 09:00").
    // Source of truth is the day_schedules collection — this column is rebuilt
    // whenever schedules change.
    @Column(name = "schedule", nullable = false, length = 200)
    private String schedule;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "capacity", nullable = false)
    @Builder.Default
    private Integer capacity = 0;

    @Column(name = "enrolled", nullable = false)
    @Builder.Default
    private Integer enrolled = 0;

    @Column(name = "visibility", nullable = false, length = 30)
    @Builder.Default
    private String visibility = "public_enrollable";

    // Admin-controlled lifecycle. Display status (open/full/ongoing/completed)
    // is derived in ClassLifecyclePolicy from this + dates + capacity.
    @Column(name = "lifecycle_status", nullable = false, length = 20)
    @Builder.Default
    private String lifecycleStatus = "draft";

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "total_sessions")
    private Integer totalSessions;

    // LinkedHashSet + @OrderBy keeps insertion / position order without tripping
    // MultipleBagFetchException when both day_schedules and course/teacher are
    // fetched eagerly via @EntityGraph.
    @OneToMany(mappedBy = "classEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private Set<ClassDaySchedule> daySchedules = new LinkedHashSet<>();
}
