package com.springjwt.module.classroom.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

// Partial update — every field nullable. `daySchedules == null` means "leave
// schedule alone"; an empty list would be rejected at the service layer.
// Lifecycle (publish/unpublish/cancel) lives on a dedicated endpoint
// (PATCH /api/classes/{id}/lifecycle), not on this DTO, so editing and
// lifecycle transitions can't get tangled in the same request.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateClassRequest {

    private Long courseId;
    private Long teacherId;

    @Size(max = 50, message = "class.validation.label.size")
    private String label;

    @Size(max = 120, message = "class.validation.location.size")
    private String location;

    @Size(max = 50, message = "class.validation.room.size")
    private String room;

    @Valid
    private List<DayScheduleInput> daySchedules;

    private LocalDate startDate;
    private LocalDate endDate;

    @Min(value = 1, message = "class.validation.capacity.min")
    private Integer capacity;

    @Pattern(regexp = "^(public_enrollable|public_view|private)$",
            message = "class.validation.visibility.invalid")
    private String visibility;
}
