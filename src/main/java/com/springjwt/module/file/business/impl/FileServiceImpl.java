package com.springjwt.module.file.business.impl;

import com.springjwt.common.exception.BadRequestException;
import com.springjwt.module.file.business.FileService;
import com.springjwt.module.file.domain.entity.FileEntity;
import com.springjwt.module.file.domain.service.FileDomainService;
import com.springjwt.module.file.domain.service.FileStorageService;
import com.springjwt.module.file.domain.service.StoredFile;
import com.springjwt.module.file.model.UploadAssetType;
import com.springjwt.module.file.model.dto.FileDto;
import com.springjwt.module.file.model.response.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileStorageService fileStorageService;
    private final FileDomainService fileDomainService;

    @Value("${file.upload.max-size:10485760}")
    private Long maxFileSize;

    @Value("${file.upload.allowed-types:jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,txt}")
    private String allowedTypes;

    @Override
    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file, String folder) {
        validateGeneric(file);
        return persistAndRespond(file, folder);
    }

    @Override
    @Transactional
    public FileUploadResponse uploadAsset(MultipartFile file, UploadAssetType assetType) {
        validateForAsset(file, assetType);
        return persistAndRespond(file, assetType.getFolder());
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadFile(String filename) {
        return fileStorageService.load(filename);
    }

    @Override
    @Transactional(readOnly = true)
    public FileDto getFileInfo(Long id) {
        FileEntity fileEntity = fileDomainService.findById(id);
        return mapToDto(fileEntity);
    }

    @Override
    @Transactional
    public void deleteFile(Long id) {
        FileEntity fileEntity = fileDomainService.findById(id);

        fileStorageService.delete(fileEntity.getStoredName());
        fileDomainService.softDelete(id);

        log.info("File deleted successfully: {}", id);
    }

    private FileUploadResponse persistAndRespond(MultipartFile file, String folder) {
        StoredFile stored = fileStorageService.store(file, folder);

        FileEntity fileEntity = FileEntity.builder()
                .originalName(file.getOriginalFilename())
                .storedName(stored.storedName())
                .filePath(stored.storedName())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .storageType(stored.storageType())
                .accessUrl(stored.accessUrl())
                .folder(folder)
                .isDeleted(false)
                .build();

        FileEntity savedFile = fileDomainService.save(fileEntity);
        log.info("File uploaded successfully: {} -> {}", savedFile.getOriginalName(), stored.accessUrl());

        return FileUploadResponse.builder()
                .id(savedFile.getId())
                .originalName(savedFile.getOriginalName())
                .storedName(savedFile.getStoredName())
                .fileSize(savedFile.getFileSize())
                .contentType(savedFile.getContentType())
                .storageType(savedFile.getStorageType())
                .accessUrl(savedFile.getAccessUrl())
                .message("File uploaded successfully")
                .build();
    }

    private void validateGeneric(MultipartFile file) {
        ensureNotEmpty(file);

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum allowed size: " + maxFileSize + " bytes");
        }

        String extension = extractExtension(file);
        List<String> allowedExtensions = Arrays.asList(allowedTypes.split(","));
        if (!allowedExtensions.contains(extension)) {
            throw new BadRequestException("File type not allowed. Allowed types: " + allowedTypes);
        }
    }

    private void validateForAsset(MultipartFile file, UploadAssetType assetType) {
        ensureNotEmpty(file);

        if (file.getSize() > assetType.getMaxSizeBytes()) {
            throw new BadRequestException(String.format(
                    "%s exceeds maximum size of %d bytes",
                    assetType.name(), assetType.getMaxSizeBytes()));
        }

        String extension = extractExtension(file);
        if (!assetType.isExtensionAllowed(extension)) {
            throw new BadRequestException(String.format(
                    "Extension '%s' not allowed for %s. Allowed: %s",
                    extension, assetType.name(), assetType.allowedExtensionsCsv()));
        }

        if (!assetType.isContentTypeAllowed(file.getContentType())) {
            throw new BadRequestException(String.format(
                    "Content type '%s' not allowed for %s",
                    file.getContentType(), assetType.name()));
        }
    }

    private void ensureNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
    }

    private String extractExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BadRequestException("Invalid file name");
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
    }

    private FileDto mapToDto(FileEntity entity) {
        return FileDto.builder()
                .id(entity.getId())
                .originalName(entity.getOriginalName())
                .storedName(entity.getStoredName())
                .filePath(entity.getFilePath())
                .fileSize(entity.getFileSize())
                .contentType(entity.getContentType())
                .storageType(entity.getStorageType())
                .accessUrl(entity.getAccessUrl())
                .folder(entity.getFolder())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }
}
