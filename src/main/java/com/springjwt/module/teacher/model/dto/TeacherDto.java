package com.springjwt.module.teacher.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherDto {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String initials;
    private String email;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private String location;
    private String bio;
    private String status;
    private BigDecimal rating;
    private int classCount;
    private int studentCount;
    private List<TeacherSubjectDto> subjects;
    private Instant createdAt;
    private Instant updatedAt;
}

