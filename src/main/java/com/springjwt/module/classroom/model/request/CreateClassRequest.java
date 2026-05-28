package com.springjwt.module.classroom.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateClassRequest {

    @NotNull(message = "class.validation.courseId.required")
    private Long courseId;

    @NotNull(message = "class.validation.teacherId.required")
    private Long teacherId;

    @NotBlank(message = "class.validation.label.required")
    @Size(max = 50, message = "class.validation.label.size")
    private String label;

    @NotBlank(message = "class.validation.location.required")
    @Size(max = 120, message = "class.validation.location.size")
    private String location;

    @Valid
    @NotNull(message = "class.validation.daySchedules.required")
    @Size(min = 1, message = "class.validation.daySchedules.min")
    @Builder.Default
    private List<DayScheduleInput> daySchedules = new ArrayList<>();

    @NotNull(message = "class.validation.startDate.required")
    private LocalDate startDate;

    @NotNull(message = "class.validation.endDate.required")
    private LocalDate endDate;

    @NotNull(message = "class.validation.capacity.required")
    @Min(value = 1, message = "class.validation.capacity.min")
    private Integer capacity;

    @NotBlank(message = "class.validation.visibility.required")
    @Pattern(regexp = "^(public_enrollable|public_view|private)$",
            message = "class.validation.visibility.invalid")
    private String visibility;
}
