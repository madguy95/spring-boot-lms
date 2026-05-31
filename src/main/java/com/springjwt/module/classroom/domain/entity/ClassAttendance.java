package com.springjwt.module.classroom.domain.entity;

import com.springjwt.common.base.entity.BaseEntity;
import com.springjwt.module.enrollment.domain.entity.Enrollment;
import jakarta.persistence.*;
import lombok.*;

/** One attendance mark for an enrolled student in a class session. */
@Entity
@Table(
        name = "class_attendance",
        uniqueConstraints = @UniqueConstraint(name = "uq_class_attendance_session_student", columnNames = {"session_id", "enrollment_id"}),
        indexes = @Index(name = "idx_class_attendance_session", columnList = "session_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassAttendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ClassSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "unmarked";

    @Column(name = "note", length = 255)
    private String note;
}
