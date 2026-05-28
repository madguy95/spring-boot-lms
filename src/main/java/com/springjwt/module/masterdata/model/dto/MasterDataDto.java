package com.springjwt.module.masterdata.model.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterDataDto {

    private Long id;
    private String type;
    private String code;
    private String name;
    private String description;
    private Integer position;
    private Boolean active;
    private Map<String, Object> metadata;
}
