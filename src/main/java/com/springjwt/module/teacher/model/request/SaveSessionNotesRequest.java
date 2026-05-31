package com.springjwt.module.teacher.model.request;

import com.springjwt.module.teacher.model.dto.TeacherSessionSummaryDto;
import com.springjwt.module.teacher.model.dto.TeacherStudentNoteDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveSessionNotesRequest {
    private TeacherSessionSummaryDto summary;
    private List<TeacherStudentNoteDto> notes;
    @Builder.Default
    private boolean sendToParents = false;
}
