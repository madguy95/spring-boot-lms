package com.springjwt.module.file.domain.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for file storage operations
 * Allows switching between different storage implementations (Local, Cloudinary, S3, Azure, etc.)
 */
public interface FileStorageService {

    /**
     * Store a file to the storage.
     *
     * @param file   MultipartFile to store
     * @param folder Optional folder path / Cloudinary folder
     * @return {@link StoredFile} containing storedName + accessUrl + storageType
     */
    StoredFile store(MultipartFile file, String folder);

    /**
     * Load a file as Resource (only meaningful for local-style storages;
     * remote CDN-backed storages may throw {@link UnsupportedOperationException}).
     *
     * @param filename File name / publicId to load
     * @return Resource
     */
    Resource load(String filename);

    /**
     * Delete a file from storage.
     *
     * @param filename File name / publicId to delete
     */
    void delete(String filename);

    /**
     * Generate access URL for a file.
     *
     * @param filename File name / publicId
     * @return Access URL
     */
    String generateAccessUrl(String filename);

    /**
     * Check if file exists.
     *
     * @param filename File name / publicId
     * @return true if exists
     */
    boolean exists(String filename);

    /**
     * Storage backend type produced by this implementation.
     */
    com.springjwt.common.enums.StorageType getStorageType();
}
