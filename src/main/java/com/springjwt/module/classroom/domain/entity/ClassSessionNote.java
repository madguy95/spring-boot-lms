package com.springjwt.module.classroom.domain.entity;

import com.springjwt.common.base.entity.BaseEntity;
import com.springjwt.module.enrollment.domain.entity.Enrollment;
import jakarta.persistence.*;
import lombok.*;

/** Per-student review note for a class session (parent-facing comment). */
@Entity
@Table(
        name = "class_session_note",
        uniqueConstraints = @UniqueConstraint(name = "uq_class_session_note_session_student", columnNames = {"session_id", "enrollment_id"}),
        indexes = @Index(name = "idx_class_session_note_session", columnList = "session_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSessionNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ClassSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(name = "attendance", nullable = false, length = 20)
    @Builder.Default
    private String attendance = "present";

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "rating", length = 20)
    private String rating;

    // Comma-separated skill tags (e.g. "Tư duy logic,Sáng tạo").
    @Column(name = "tags", length = 255)
    private String tags;

    @Column(name = "saved", nullable = false)
    @Builder.Default
    private Boolean saved = false;
}
