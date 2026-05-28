package com.springjwt.module.course.model.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DuplicateCourseRequest {

    @Size(max = 50, message = "course.validation.code.size")
    private String code;

    @Size(max = 200, message = "course.validation.title.size")
    private String title;
}
