package com.springjwt.module.file.business.impl;

import com.springjwt.common.enums.StorageType;
import com.springjwt.common.exception.BadRequestException;
import com.springjwt.module.file.business.FileService;
import com.springjwt.module.file.domain.entity.FileEntity;
import com.springjwt.module.file.domain.service.FileDomainService;
import com.springjwt.module.file.domain.service.FileStorageService;
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
        validateFile(file);

        String storedName = fileStorageService.store(file, folder);
        String accessUrl = fileStorageService.generateAccessUrl(storedName);

        FileEntity fileEntity = FileEntity.builder()
                .originalName(file.getOriginalFilename())
                .storedName(storedName)
                .filePath(storedName)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .storageType(StorageType.LOCAL)
                .accessUrl(accessUrl)
                .folder(folder)
                .isDeleted(false)
                .build();

        FileEntity savedFile = fileDomainService.save(fileEntity);
        log.info("File uploaded successfully: {}", savedFile.getOriginalName());

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

        // Delete from storage
        fileStorageService.delete(fileEntity.getStoredName());

        // Soft delete from database
        fileDomainService.softDelete(id);

        log.info("File deleted successfully: {}", id);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum allowed size: " + maxFileSize + " bytes");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BadRequestException("Invalid file name");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedExtensions = Arrays.asList(allowedTypes.split(","));

        if (!allowedExtensions.contains(extension)) {
            throw new BadRequestException("File type not allowed. Allowed types: " + allowedTypes);
        }
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

