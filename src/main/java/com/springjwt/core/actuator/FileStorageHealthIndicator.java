package com.springjwt.core.actuator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Custom Health Indicator for file storage
 * Checks if upload directory is accessible and has enough space
 * Only runs when storage type is LOCAL
 */
@Component("fileStorage")
public class FileStorageHealthIndicator implements HealthIndicator {

    private static final long MIN_DISK_SPACE = 100 * 1024 * 1024; // 100MB

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.storage.type:LOCAL}")
    private String storageType;

    @Override
    public Health health() {
        // Only check when storage type is LOCAL
        if (!"LOCAL".equalsIgnoreCase(storageType)) {
            return Health.up()
                    .withDetail("storageType", storageType)
                    .withDetail("message", "Health check skipped for non-local storage")
                    .build();
        }

        try {
            File uploadDirectory = new File(uploadDir);

            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }

            long usableSpace = uploadDirectory.getUsableSpace();
            long totalSpace = uploadDirectory.getTotalSpace();
            long freeSpace = uploadDirectory.getFreeSpace();

            if (usableSpace < MIN_DISK_SPACE) {
                return Health.down()
                        .withDetail("error", "Low disk space")
                        .withDetail("storageType", storageType)
                        .withDetail("uploadDirectory", uploadDirectory.getAbsolutePath())
                        .withDetail("usableSpace", formatBytes(usableSpace))
                        .withDetail("totalSpace", formatBytes(totalSpace))
                        .withDetail("freeSpace", formatBytes(freeSpace))
                        .withDetail("minimumRequired", formatBytes(MIN_DISK_SPACE))
                        .build();
            }

            return Health.up()
                    .withDetail("storageType", storageType)
                    .withDetail("uploadDirectory", uploadDirectory.getAbsolutePath())
                    .withDetail("usableSpace", formatBytes(usableSpace))
                    .withDetail("totalSpace", formatBytes(totalSpace))
                    .withDetail("freeSpace", formatBytes(freeSpace))
                    .withDetail("writable", uploadDirectory.canWrite())
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("storageType", storageType)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
}

