package com.springjwt.module.file.api;

import com.springjwt.common.base.response.ApiResult;
import com.springjwt.module.file.business.FileService;
import com.springjwt.module.file.model.dto.FileDto;
import com.springjwt.module.file.model.response.FileUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Management", description = "File upload, download, and management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "Upload file", description = "Upload a file to the server with optional folder specification.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File uploaded successfully",
                    content = @Content(schema = @Schema(implementation = FileUploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or upload failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ApiResult<FileUploadResponse> uploadFile(
            @Parameter(description = "File to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Optional folder path")
            @RequestParam(value = "folder", required = false) String folder) {
        log.info("Upload file request: {}, folder: {}", file.getOriginalFilename(), folder);
        FileUploadResponse response = fileService.uploadFile(file, folder);
        return ApiResult.success(response);
    }

    @GetMapping("/{filename:.+}")
    @Operation(summary = "Download file", description = "Download a file by filename.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "Filename to download", required = true)
            @PathVariable String filename) {
        log.info("Download file request: {}", filename);
        Resource resource = fileService.downloadFile(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/info/{id}")
    @Operation(summary = "Get file info", description = "Retrieve file metadata by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File info retrieved",
                    content = @Content(schema = @Schema(implementation = FileDto.class))),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ApiResult<FileDto> getFileInfo(
            @Parameter(description = "File ID", required = true)
            @PathVariable Long id) {
        log.info("Get file info request: {}", id);
        FileDto fileDto = fileService.getFileInfo(id);
        return ApiResult.success(fileDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete file", description = "Delete a file by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File deleted successfully"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ApiResult<String> deleteFile(
            @Parameter(description = "File ID to delete", required = true)
            @PathVariable Long id) {
        log.info("Delete file request: {}", id);
        fileService.deleteFile(id);
        return ApiResult.success("File deleted successfully");
    }
}


