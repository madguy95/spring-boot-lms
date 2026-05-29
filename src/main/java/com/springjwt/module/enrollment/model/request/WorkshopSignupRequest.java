package com.springjwt.module.enrollment.model.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Lightweight payload for the workshop quick-signup form on the public blog
 * detail page. Designed to be the absolute minimum a parent needs to fill out
 * so an admin can call back and confirm — anything optional belongs in `note`
 * to keep the form short.
 *
 * <p>The companion endpoint ({@code POST /api/public/enrollments/workshop-signup})
 * derives {@code requestedCourseId} from the linked class and sets
 * {@code channel='website_workshop'} so admins can triage these rows separately.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkshopSignupRequest {

    /** Workshop class the parent picked. Required. */
    @NotNull(message = "enrollment.validation.intendedClass.required")
    private Long classId;

    @NotBlank(message = "enrollment.validation.parentName.required")
    @Size(max = 120, message = "enrollment.validation.parentName.size")
    private String parentName;

    @NotBlank(message = "enrollment.validation.parentPhone.required")
    @Pattern(regexp = "^[0-9+\\-\\s]{8,20}$", message = "enrollment.validation.parentPhone.invalid")
    private String parentPhone;

    @NotBlank(message = "enrollment.validation.studentName.required")
    @Size(max = 120, message = "enrollment.validation.studentName.size")
    private String studentName;

    @NotNull(message = "enrollment.validation.studentAge.required")
    @Min(value = 0, message = "enrollment.validation.studentAge.min")
    @Max(value = 30, message = "enrollment.validation.studentAge.max")
    private Integer studentAge;

    @Size(max = 500, message = "enrollment.validation.note.size")
    private String note;
}
