package com.springjwt.module.file.model.response;

import com.springjwt.common.enums.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadResponse {
    private Long id;
    private String originalName;
    private String storedName;
    private Long fileSize;
    private String contentType;
    private StorageType storageType;
    private String accessUrl;
    private String message;
}

