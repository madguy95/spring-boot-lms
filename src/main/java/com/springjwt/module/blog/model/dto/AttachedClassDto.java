package com.springjwt.module.blog.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Joined snapshot of a Class + its Course, shaped for the workshop sidebar on
 * the public blog detail page. Returned inline on BlogPost responses (workshop
 * type only) so the FE doesn't need a second round-trip to render the schedule
 * + capacity card.
 *
 * <p>This is read-only — admins edit logistics through the Class admin UI; the
 * blog post never owns these fields.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttachedClassDto {
    private Long id;
    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private String schedule;     // composed label e.g. "T2/T4 · 18:00"
    private String location;
    private String room;
    private Integer minAge;
    private Integer maxAge;
    private Integer capacity;
    private Integer enrolled;
    private BigDecimal tuitionAmount;
    private String status;       // class display status: open/full/ongoing/...
    /**
     * ISO instant for the next session — used by the public landing feed to
     * sort + filter "upcoming workshops". Computed as the first daySchedule
     * occurrence ≥ today.
     */
    private Instant startsAt;
}
