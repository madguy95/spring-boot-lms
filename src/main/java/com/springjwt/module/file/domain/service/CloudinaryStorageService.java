package com.springjwt.module.file.domain.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.springjwt.common.enums.StorageType;
import com.springjwt.common.exception.AppException;
import com.springjwt.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Cloudinary-backed implementation of {@link FileStorageService}.
 *
 * <p>The {@code storedName} returned from {@link #store(MultipartFile, String)} is the Cloudinary
 * {@code publicId} (already including the folder prefix, e.g. {@code lms/teacher-avatars/abc123}).
 * Persist this value as it is the only identifier needed to {@link #delete(String) delete} the
 * resource later.
 *
 * <p>The resource type ({@code image} / {@code video} / {@code raw}) is detected from the file's
 * content type so the same {@code FileService} flow works for avatars, cover images, and intro
 * videos without callers needing to pass extra parameters.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "file.storage", name = "type", havingValue = "cloudinary")
public class CloudinaryStorageService implements FileStorageService {

    private final Cloudinary cloudinary;

    @Value("${file.cloudinary.default-folder:lms}")
    private String defaultFolder;

    @Override
    public StoredFile store(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file");
        }

        String resourceType = detectResourceType(file.getContentType());
        String targetFolder = (folder != null && !folder.isBlank())
                ? (defaultFolder + "/" + folder)
                : defaultFolder;

        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", targetFolder,
                    "resource_type", resourceType,
                    "use_filename", true,
                    "unique_filename", true,
                    "overwrite", false
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);

            String publicId = (String) result.get("public_id");
            String secureUrl = (String) result.get("secure_url");

            if (publicId == null || secureUrl == null) {
                throw new AppException("Cloudinary upload returned no publicId/secure_url");
            }

            log.info("Cloudinary upload successful: publicId={}, resourceType={}", publicId, resourceType);
            return new StoredFile(publicId, secureUrl, StorageType.CLOUDINARY);

        } catch (IOException e) {
            throw new AppException("Failed to upload file to Cloudinary", e);
        }
    }

    @Override
    public Resource load(String filename) {
        // Cloudinary is a CDN - clients should hit the accessUrl directly.
        throw new UnsupportedOperationException(
                "Cloudinary files are served via CDN URL; use accessUrl instead of /api/files/{name}");
    }

    @Override
    public void delete(String filename) {
        // publicId may belong to image OR video; try image first, then video, then raw.
        for (String type : new String[]{"image", "video", "raw"}) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = cloudinary.uploader().destroy(filename,
                        ObjectUtils.asMap("resource_type", type, "invalidate", true));
                Object outcome = result.get("result");
                if ("ok".equals(outcome)) {
                    log.info("Cloudinary delete successful: publicId={}, resourceType={}", filename, type);
                    return;
                }
            } catch (IOException e) {
                log.warn("Cloudinary delete attempt failed for {} as {}: {}", filename, type, e.getMessage());
            }
        }
        log.warn("Cloudinary resource not found for delete: {}", filename);
    }

    @Override
    public String generateAccessUrl(String filename) {
        // For Cloudinary the secure URL is captured at upload time; rebuilding here would require
        // knowing the resource type. Callers that have an existing FileEntity should read
        // accessUrl from the persisted record instead of calling this method.
        return cloudinary.url().secure(true).generate(filename);
    }

    @Override
    public boolean exists(String filename) {
        for (String type : new String[]{"image", "video", "raw"}) {
            try {
                cloudinary.api().resource(filename, ObjectUtils.asMap("resource_type", type));
                return true;
            } catch (Exception ignored) {
                // try next resource type
            }
        }
        return false;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.CLOUDINARY;
    }

    private String detectResourceType(String contentType) {
        if (contentType == null) {
            return "auto";
        }
        if (contentType.startsWith("image/")) {
            return "image";
        }
        if (contentType.startsWith("video/")) {
            return "video";
        }
        return "raw";
    }
}
