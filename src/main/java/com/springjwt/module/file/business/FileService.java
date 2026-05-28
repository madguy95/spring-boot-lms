package com.springjwt.module.file.business;

import com.springjwt.module.file.model.UploadAssetType;
import com.springjwt.module.file.model.dto.FileDto;
import com.springjwt.module.file.model.response.FileUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * Generic upload with optional folder. Uses the default app-wide validation rules.
     */
    FileUploadResponse uploadFile(MultipartFile file, String folder);

    /**
     * Business-typed upload (teacher avatar, course cover, course intro video, ...).
     * The asset type drives folder, allowed extensions, content type, and max file size.
     */
    FileUploadResponse uploadAsset(MultipartFile file, UploadAssetType assetType);

    Resource downloadFile(String filename);

    FileDto getFileInfo(Long id);

    void deleteFile(Long id);
}
