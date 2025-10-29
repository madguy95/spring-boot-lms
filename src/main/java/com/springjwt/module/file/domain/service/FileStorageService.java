package com.springjwt.module.file.domain.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for file storage operations
 * Allows switching between different storage implementations (Local, S3, Azure, etc.)
 */
public interface FileStorageService {

    /**
     * Store a file to the storage
     * @param file MultipartFile to store
     * @param folder Optional folder path
     * @return Stored file name
     */
    String store(MultipartFile file, String folder);

    /**
     * Load a file as Resource
     * @param filename File name to load
     * @return Resource
     */
    Resource load(String filename);

    /**
     * Delete a file from storage
     * @param filename File name to delete
     */
    void delete(String filename);

    /**
     * Generate access URL for a file
     * @param filename File name
     * @return Access URL
     */
    String generateAccessUrl(String filename);

    /**
     * Check if file exists
     * @param filename File name
     * @return true if exists
     */
    boolean exists(String filename);
}

