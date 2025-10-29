package com.springjwt.module.audit.api;

import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.common.base.response.ApiResult;
import com.springjwt.common.base.response.PagedResult;
import com.springjwt.module.audit.business.AuditLogService;
import com.springjwt.module.audit.model.dto.AuditLogDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit Logs", description = "Audit log query APIs (Admin only)")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/user/{username}")
    @Operation(summary = "Get user audit logs", description = "Retrieve paginated audit logs for a specific user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved",
                    content = @Content(schema = @Schema(implementation = PagedResult.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    public ResponseEntity<PagedResult<AuditLogDto>> getUserAuditLogs(
            @Parameter(description = "Username", required = true)
            @PathVariable String username,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLogDto> auditLogs = auditLogService.getUserAuditLogs(username, pageable);

        return ResponseFactory.success(auditLogs);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get audit logs by date range", description = "Retrieve audit logs within a specific date range.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    public ResponseEntity<PagedResult<AuditLogDto>> getAuditLogsByDateRange(
            @Parameter(description = "Start time (ISO-8601 format)", required = true, example = "2023-01-01T00:00:00Z")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @Parameter(description = "End time (ISO-8601 format)", required = true, example = "2023-12-31T23:59:59Z")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLogDto> auditLogs = auditLogService.getAuditLogsByDateRange(startTime, endTime, pageable);

        return ResponseFactory.success(auditLogs);
    }

    @GetMapping("/correlation/{correlationId}")
    @Operation(summary = "Get audit logs by correlation ID", description = "Retrieve all audit logs for a specific request using correlation ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    public ApiResult<List<AuditLogDto>> getAuditLogsByCorrelationId(
            @Parameter(description = "Correlation ID", required = true)
            @PathVariable String correlationId) {

        List<AuditLogDto> auditLogs = auditLogService.getAuditLogsByCorrelationId(correlationId);
        return ApiResult.success(auditLogs);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get entity history", description = "Retrieve all changes made to a specific entity.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entity history retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    public ApiResult<List<AuditLogDto>> getEntityHistory(
            @Parameter(description = "Entity type", required = true, example = "User")
            @PathVariable String entityType,
            @Parameter(description = "Entity ID", required = true, example = "123")
            @PathVariable String entityId) {

        List<AuditLogDto> history = auditLogService.getEntityHistory(entityType, entityId);
        return ApiResult.success(history);
    }

    @GetMapping("/user/{username}/recent")
    @Operation(summary = "Get user recent activity", description = "Retrieve user's recent activity within specified days.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent activity retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    public ApiResult<List<AuditLogDto>> getUserRecentActivity(
            @Parameter(description = "Username", required = true)
            @PathVariable String username,
            @Parameter(description = "Number of days to look back", example = "7")
            @RequestParam(defaultValue = "7") int days) {

        List<AuditLogDto> activity = auditLogService.getUserRecentActivity(username, days);
        return ApiResult.success(activity);
    }
}