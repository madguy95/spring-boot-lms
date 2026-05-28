package com.springjwt.module.file.api;

import com.springjwt.common.base.response.ApiResult;
import com.springjwt.module.file.business.FileService;
import com.springjwt.module.file.model.UploadAssetType;
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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file (generic)",
            description = "Generic upload endpoint with optional folder. Uses default app-wide validation rules.")
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

    @PostMapping(value = "/upload/teacher-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload teacher avatar",
            description = "Upload a teacher avatar image. Allowed: jpg/jpeg/png/webp, max 5MB.")
    public ApiResult<FileUploadResponse> uploadTeacherAvatar(
            @Parameter(description = "Avatar image", required = true)
            @RequestParam("file") MultipartFile file) {
        log.info("Upload teacher-avatar request: {}", file.getOriginalFilename());
        return ApiResult.success(fileService.uploadAsset(file, UploadAssetType.TEACHER_AVATAR));
    }

    @PostMapping(value = "/upload/course-cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload course cover image",
            description = "Upload a course cover image. Allowed: jpg/jpeg/png/webp, max 10MB.")
    public ApiResult<FileUploadResponse> uploadCourseCover(
            @Parameter(description = "Cover image", required = true)
            @RequestParam("file") MultipartFile file) {
        log.info("Upload course-cover request: {}", file.getOriginalFilename());
        return ApiResult.success(fileService.uploadAsset(file, UploadAssetType.COURSE_COVER));
    }

    @PostMapping(value = "/upload/course-intro-video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload course intro video",
            description = "Upload a course intro video. Allowed: mp4/mov/webm/mkv, max 200MB.")
    public ApiResult<FileUploadResponse> uploadCourseIntroVideo(
            @Parameter(description = "Intro video", required = true)
            @RequestParam("file") MultipartFile file) {
        log.info("Upload course-intro-video request: {}", file.getOriginalFilename());
        return ApiResult.success(fileService.uploadAsset(file, UploadAssetType.COURSE_INTRO_VIDEO));
    }

    @GetMapping("/{filename:.+}")
    @Operation(summary = "Download file",
            description = "Download a file by filename. Only meaningful for LOCAL storage; for Cloudinary, use the accessUrl directly.")
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
    @Operation(summary = "Delete file", description = "Delete a file by ID. Removes both DB record (soft delete) and the underlying storage object.")
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
