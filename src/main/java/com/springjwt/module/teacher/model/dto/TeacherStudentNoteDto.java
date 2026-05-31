package com.springjwt.module.teacher.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherStudentNoteDto {
    private String studentId;    // enrollment id as string
    private String attendance;   // present|excused|absent|makeup|unmarked
    private String note;
    private String rating;       // weak|average|good|great|excellent (nullable)
    private List<String> tags;
    private Boolean saved;
}
