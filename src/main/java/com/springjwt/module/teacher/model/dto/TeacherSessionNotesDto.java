package com.springjwt.module.teacher.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherSessionNotesDto {
    private String sessionId;
    private TeacherSessionMetaDto meta;
    private TeacherSessionSummaryDto summary;
    private List<TeacherStudentNoteDto> notes;
}
