package com.springjwt.module.course.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "course_discounts", indexes = {
        @Index(name = "idx_course_discounts_course", columnList = "course_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    // percentage | fixed | special
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    // Populated when type is percentage or fixed.
    @Column(name = "value_numeric", precision = 15, scale = 2)
    private BigDecimal valueNumeric;

    // Populated when type is special (free-form text, e.g. "Free trial week").
    @Column(name = "value_text", length = 255)
    private String valueText;

    // none | before_date | has_sibling | trial_only
    @Column(name = "condition", nullable = false, length = 30)
    @Builder.Default
    private String condition = "none";

    @Column(name = "condition_date")
    private LocalDate conditionDate;

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
