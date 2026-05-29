package com.springjwt.module.file.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Business-level upload categories. Each value carries its own folder, allowed extensions
 * and size limit so callers (controllers, services) don't duplicate validation rules.
 *
 * <p>Adding a new asset type? Add an entry here and expose an endpoint in
 * {@link com.springjwt.module.file.api.FileController}.
 */
@Getter
public enum UploadAssetType {

    TEACHER_AVATAR(
            "teacher-avatars",
            List.of("jpg", "jpeg", "png", "webp"),
            List.of("image/"),
            5L * 1024 * 1024), // 5MB

    COURSE_COVER(
            "course-covers",
            List.of("jpg", "jpeg", "png", "webp"),
            List.of("image/"),
            10L * 1024 * 1024), // 10MB

    COURSE_INTRO_VIDEO(
            "course-intro-videos",
            List.of("mp4", "mov", "webm", "mkv"),
            List.of("video/"),
            200L * 1024 * 1024), // 200MB

    BLOG_COVER(
            "blog-covers",
            List.of("jpg", "jpeg", "png", "webp"),
            List.of("image/"),
            5L * 1024 * 1024); // 5MB — matches the FE dropzone limit

    private final String folder;
    private final List<String> allowedExtensions;
    private final List<String> allowedContentTypePrefixes;
    private final long maxSizeBytes;

    UploadAssetType(String folder,
                    List<String> allowedExtensions,
                    List<String> allowedContentTypePrefixes,
                    long maxSizeBytes) {
        this.folder = folder;
        this.allowedExtensions = allowedExtensions;
        this.allowedContentTypePrefixes = allowedContentTypePrefixes;
        this.maxSizeBytes = maxSizeBytes;
    }

    public boolean isExtensionAllowed(String extension) {
        return extension != null && allowedExtensions.contains(extension.toLowerCase());
    }

    public boolean isContentTypeAllowed(String contentType) {
        if (contentType == null) {
            return false;
        }
        return allowedContentTypePrefixes.stream().anyMatch(contentType::startsWith);
    }

    public String allowedExtensionsCsv() {
        return String.join(",", allowedExtensions);
    }

    public static UploadAssetType fromString(String raw) {
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(raw))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown upload asset type: " + raw));
    }
}
