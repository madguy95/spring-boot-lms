package com.springjwt.module.course.domain.entity;

import com.springjwt.common.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "courses", indexes = {
        @Index(name = "idx_courses_status", columnList = "status"),
        @Index(name = "idx_courses_tool", columnList = "tool")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "tagline", length = 200)
    private String tagline;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "tool", nullable = false, length = 50)
    private String tool;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "draft";

    @Column(name = "min_age", nullable = false)
    private Integer minAge;

    @Column(name = "max_age", nullable = false)
    private Integer maxAge;

    @Column(name = "total_sessions", nullable = false)
    @Builder.Default
    private Integer totalSessions = 0;

    @Column(name = "session_duration_minutes", nullable = false)
    @Builder.Default
    private Integer sessionDurationMinutes = 0;

    @Column(name = "per_class_capacity", nullable = false)
    @Builder.Default
    private Integer perClassCapacity = 0;

    @Column(name = "tuition_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal tuitionAmount = BigDecimal.ZERO;

    @Column(name = "original_tuition_amount", precision = 15, scale = 2)
    private BigDecimal originalTuitionAmount;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "intro_video_url", length = 500)
    private String introVideoUrl;

    @Column(name = "pricing_notes", columnDefinition = "TEXT")
    private String pricingNotes;

    // User-facing version tag (e.g. "v0.1"). Distinct from BaseEntity.version (optimistic lock).
    @Column(name = "version_label", nullable = false, length = 20)
    @Builder.Default
    private String versionLabel = "v0.1";

    // Set (LinkedHashSet via @OrderBy) instead of List to avoid Hibernate's
    // MultipleBagFetchException when both collections are eagerly fetched together.
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private Set<CourseSession> sessions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private Set<CourseDiscount> discounts = new LinkedHashSet<>();
}
