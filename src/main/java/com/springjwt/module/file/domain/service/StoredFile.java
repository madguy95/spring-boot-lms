package com.springjwt.module.file.domain.service;

import com.springjwt.common.enums.StorageType;

/**
 * Result of a successful file upload to a {@link FileStorageService}.
 *
 * @param storedName  identifier used to later retrieve / delete the file
 *                    (local: relative path under upload dir; cloudinary: publicId with folder)
 * @param accessUrl   public URL clients should use to access the file
 * @param storageType storage backend that produced this file
 */
public record StoredFile(String storedName, String accessUrl, StorageType storageType) {
}
