package com.springjwt.module.enrollment.domain.entity;

import com.springjwt.common.base.entity.BaseEntity;
import com.springjwt.module.classroom.domain.entity.ClassEntity;
import com.springjwt.module.course.domain.entity.Course;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "enrollments", indexes = {
        @Index(name = "idx_enrollments_status", columnList = "status"),
        @Index(name = "idx_enrollments_course_id", columnList = "requested_course_id"),
        @Index(name = "idx_enrollments_class_id", columnList = "assigned_class_id"),
        @Index(name = "idx_enrollments_submitted_at", columnList = "submitted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_name", nullable = false, length = 120)
    private String studentName;

    @Column(name = "student_age")
    private Integer studentAge;

    @Column(name = "student_grade")
    private Integer studentGrade;

    @Column(name = "parent_name", nullable = false, length = 120)
    private String parentName;

    @Column(name = "parent_phone", length = 20)
    private String parentPhone;

    @Column(name = "parent_email", length = 120)
    private String parentEmail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_course_id", nullable = false)
    private Course requestedCourse;

    // Populated only after admin approves + assigns the enrollment to a class.
    // Null while the row is pending / waitlist / rejected.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_class_id")
    private ClassEntity assignedClass;

    // Set by guests on the workshop quick-signup form to record which class
    // they were looking at when they submitted. Separate from `assigned_class_id`
    // so the admin's approval flow stays intact — they can confirm the same
    // class or reroute to another one.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intended_class_id")
    private ClassEntity intendedClass;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "pending";

    @Column(name = "channel", nullable = false, length = 20)
    @Builder.Default
    private String channel = "parent_app";

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "payment_amount", precision = 15, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus;

    @Column(name = "submitted_at", nullable = false)
    @Builder.Default
    private Instant submittedAt = Instant.now();

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "waitlisted_at")
    private Instant waitlistedAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;
}
