package com.springjwt.module.file.model.dto;

import com.springjwt.common.enums.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDto {
    private Long id;
    private String originalName;
    private String storedName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private StorageType storageType;
    private String accessUrl;
    private String folder;
    private Instant createdAt;
    private String createdBy;
}

