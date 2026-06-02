package com.springjwt.module.consultation.domain.entity;

import com.springjwt.common.base.entity.BaseEntity;
import com.springjwt.module.course.domain.entity.Course;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "consultation_requests", indexes = {
        @Index(name = "idx_consultation_status",     columnList = "status"),
        @Index(name = "idx_consultation_created_at", columnList = "created_at"),
        @Index(name = "idx_consultation_course_id",  columnList = "interested_course_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_name", nullable = false, length = 120)
    private String parentName;

    @Column(name = "parent_phone", nullable = false, length = 20)
    private String parentPhone;

    @Column(name = "child_name", length = 120)
    private String childName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interested_course_id")
    private Course interestedCourse;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "new";
}
