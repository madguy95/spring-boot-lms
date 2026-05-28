package com.springjwt.module.teacher.domain.entity;

import com.springjwt.common.base.entity.BaseEntity;
import com.springjwt.module.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teachers", indexes = {
        @Index(name = "idx_teachers_status", columnList = "status"),
        @Index(name = "idx_teachers_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "location", length = 120)
    private String location;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "pending";

    @Column(name = "rating", nullable = false, precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<TeacherSubject> teacherSubjects = new HashSet<>();
}

