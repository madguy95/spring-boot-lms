package com.springjwt.module.teacher.model.request;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveSessionAttendanceRequest {
    /** enrollmentId(string) -> status (present|excused|absent|makeup|unmarked). */
    private Map<String, String> marks;

    /** enrollmentId(string) -> free-text attendance note. Optional. */
    private Map<String, String> notes;
}
