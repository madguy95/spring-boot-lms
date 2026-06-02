package com.springjwt.module.teacher.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class PublicTeacherDto {

    private Long id;
    private String fullName;
    private String initials;
    private String avatarUrl;
    private List<String> subjects;
    private String bio;
    private int studentCount;
    private BigDecimal rating;
}
