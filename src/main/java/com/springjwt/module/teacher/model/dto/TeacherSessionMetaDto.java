package com.springjwt.module.teacher.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherSessionMetaDto {
    private String id;          // session code
    private String dayLabel;
    private String title;
    private String description;
    private String timeLabel;
}
