package com.springjwt.module.enrollment.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkActionResultDto {
    private int updated;
    private int skipped;
    private List<Long> updatedIds;
    private List<Long> skippedIds;
}
