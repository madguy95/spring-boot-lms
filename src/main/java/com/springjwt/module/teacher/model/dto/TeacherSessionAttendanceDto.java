package com.springjwt.module.teacher.model.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherSessionAttendanceDto {
    private String sessionId;
    private TeacherSessionMetaDto meta;
    /** enrollmentId(string) -> status (every roster student present in the map). */
    private Map<String, String> marks;
    /** enrollmentId(string) -> free-text attendance note (only non-empty notes). */
    private Map<String, String> notes;
}
