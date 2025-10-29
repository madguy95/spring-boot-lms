package com.springjwt.module.file.domain.entity;

import com.springjwt.common.base.entity.BaseEntity;
import com.springjwt.common.enums.StorageType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "stored_name", nullable = false, unique = true)
    private String storedName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false)
    private StorageType storageType;

    @Column(name = "access_url")
    private String accessUrl;

    @Column(name = "folder")
    private String folder;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
}

