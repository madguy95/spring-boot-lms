package com.springjwt.module.teacher.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teacher_subjects", indexes = {
        @Index(name = "idx_teacher_subjects_subject", columnList = "subject_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherSubject {

    @EmbeddedId
    private TeacherSubjectId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("teacherId")
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("subjectId")
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;
}

