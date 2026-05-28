package com.springjwt.module.course.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseDiscountDto {
    private String name;
    private String type;       // percentage | fixed | special
    // For percentage/fixed this is the numeric amount (as string to preserve precision);
    // for special this is the free-form text. Frontend treats both via the same `value` field.
    private Object value;
    private String condition;
    private LocalDate conditionDate;
}
