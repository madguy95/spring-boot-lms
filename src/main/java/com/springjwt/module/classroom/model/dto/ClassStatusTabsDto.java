package com.springjwt.module.classroom.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassStatusTabsDto {
    private long all;
    private long draft;
    private long open;
    private long full;
    private long ongoing;
    private long completed;
    private long unpublished;
    private long cancelled;
}
