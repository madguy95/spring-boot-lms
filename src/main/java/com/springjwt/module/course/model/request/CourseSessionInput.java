package com.springjwt.module.course.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSessionInput {

    @NotBlank(message = "course.validation.session.title.required")
    @Size(max = 200, message = "course.validation.session.title.size")
    private String title;

    private String description;
}
