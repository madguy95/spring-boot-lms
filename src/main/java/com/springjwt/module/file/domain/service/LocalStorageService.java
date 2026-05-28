package com.springjwt.module.file.domain.service;

import com.springjwt.common.enums.StorageType;
import com.springjwt.common.exception.AppException;
import com.springjwt.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "file.storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements FileStorageService {

    private final Path rootLocation;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    public LocalStorageService(@Value("${file.upload.dir:uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(this.rootLocation);
            log.info("Storage location initialized: {}", this.rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new AppException("Could not initialize storage location", e);
        }
    }

    @Override
    public StoredFile store(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String storedName = UUID.randomUUID().toString() + extension;

            Path destinationPath = this.rootLocation;
            if (folder != null && !folder.isBlank()) {
                destinationPath = destinationPath.resolve(folder);
                Files.createDirectories(destinationPath);
            }

            Path destinationFile = destinationPath.resolve(storedName).normalize().toAbsolutePath();

            if (!destinationFile.getParent().startsWith(this.rootLocation.toAbsolutePath())) {
                throw new BadRequestException("Cannot store file outside current directory");
            }

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            String finalName = (folder != null && !folder.isBlank()) ? folder + "/" + storedName : storedName;
            log.info("File stored successfully: {}", finalName);
            return new StoredFile(finalName, generateAccessUrl(finalName), StorageType.LOCAL);

        } catch (IOException e) {
            throw new AppException("Failed to store file", e);
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new BadRequestException("Could not read file: " + filename);
            }
        } catch (IOException e) {
            throw new AppException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Files.deleteIfExists(file);
            log.info("File deleted successfully: {}", filename);
        } catch (IOException e) {
            throw new AppException("Failed to delete file: " + filename, e);
        }
    }

    @Override
    public String generateAccessUrl(String filename) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(contextPath)
                .path("/api/files/")
                .path(filename)
                .toUriString();
    }

    @Override
    public boolean exists(String filename) {
        Path file = rootLocation.resolve(filename).normalize();
        return Files.exists(file);
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.LOCAL;
    }
}
