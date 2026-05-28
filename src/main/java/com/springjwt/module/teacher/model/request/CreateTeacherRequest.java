package com.springjwt.module.teacher.model.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTeacherRequest {

    @NotBlank(message = "teacher.validation.firstName.required")
    @Size(max = 50, message = "teacher.validation.firstName.size")
    private String firstName;

    @NotBlank(message = "teacher.validation.lastName.required")
    @Size(max = 50, message = "teacher.validation.lastName.size")
    private String lastName;

    @NotBlank(message = "teacher.validation.email.required")
    @Email(message = "teacher.validation.email.invalid")
    @Size(max = 50, message = "teacher.validation.email.size")
    private String email;

    @NotBlank(message = "teacher.validation.phone.required")
    @Pattern(regexp = "^[0-9+\\-\\s]{8,20}$", message = "teacher.validation.phone.invalid")
    private String phone;

    @NotBlank(message = "teacher.validation.username.required")
    @Size(max = 20, message = "teacher.validation.username.size")
    private String username;

    @Pattern(regexp = "^(male|female|other|prefer_not_to_say)$", message = "teacher.validation.gender.invalid")
    private String gender;

    @Past(message = "teacher.validation.dateOfBirth.past")
    private LocalDate dateOfBirth;

    @Size(max = 120, message = "teacher.validation.location.size")
    private String location;

    private String bio;

    @Size(max = 255, message = "teacher.validation.avatarUrl.size")
    private String avatarUrl;

    @NotNull(message = "teacher.validation.primarySubject.required")
    private Long primarySubjectId;

    @NotEmpty(message = "teacher.validation.subjectIds.required")
    private Set<Long> subjectIds;

    @Builder.Default
    private Boolean sendOnboardingEmail = false;
}

