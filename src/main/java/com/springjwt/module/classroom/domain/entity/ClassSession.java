package com.springjwt.module.classroom.domain.entity;

import com.springjwt.common.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * A single taught/upcoming session of a class (B1..Bn) for the Teacher
 * "Class detail" screen. The whole-class per-session review summary is stored
 * inline ({@code summaryComment} / {@code summaryRating}) to avoid a 4th table.
 */
@Entity
@Table(
        name = "class_session",
        uniqueConstraints = @UniqueConstraint(name = "uq_class_session_class_code", columnNames = {"class_id", "code"}),
        indexes = @Index(name = "idx_class_session_class", columnList = "class_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @Column(name = "idx", nullable = false)
    private Integer idx;

    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Column(name = "session_date")
    private LocalDate sessionDate;

    @Column(name = "date_label", length = 40)
    private String dateLabel;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "upcoming";

    @Column(name = "time_label", length = 80)
    private String timeLabel;

    @Column(name = "day_label", length = 80)
    private String dayLabel;

    @Column(name = "summary_comment", columnDefinition = "TEXT")
    private String summaryComment;

    @Column(name = "summary_rating", length = 20)
    private String summaryRating;
}
