package com.springjwt.module.classroom.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleDayDto {
    // "short" is a Java keyword, so we expose the field as "short" via Jackson
    // while keeping the Java field named shortName.
    @com.fasterxml.jackson.annotation.JsonProperty("short")
    private String shortName;
    private int date;
    private Boolean isToday;
    private LocalDate isoDate;
}
