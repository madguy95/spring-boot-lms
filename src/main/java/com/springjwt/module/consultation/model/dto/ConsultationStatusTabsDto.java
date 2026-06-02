package com.springjwt.module.consultation.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationStatusTabsDto {
    private long all;
    private long newCount;
    private long contacted;
    private long enrolled;
    private long notInterested;
}
