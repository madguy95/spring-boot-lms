package com.springjwt.module.file.business;

import com.springjwt.module.file.model.dto.FileDto;
import com.springjwt.module.file.model.response.FileUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileUploadResponse uploadFile(MultipartFile file, String folder);

    Resource downloadFile(String filename);

    FileDto getFileInfo(Long id);

    void deleteFile(Long id);
}

