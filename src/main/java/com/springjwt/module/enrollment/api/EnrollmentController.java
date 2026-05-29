package com.springjwt.module.enrollment.api;

import com.springjwt.common.base.response.ApiResult;
import com.springjwt.common.base.response.PagedResult;
import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.module.enrollment.business.EnrollmentService;
import com.springjwt.module.enrollment.model.dto.BulkActionResultDto;
import com.springjwt.module.enrollment.model.dto.EnrollmentDto;
import com.springjwt.module.enrollment.model.dto.EnrollmentStatusTabsDto;
import com.springjwt.module.enrollment.model.request.ApproveEnrollmentRequest;
import com.springjwt.module.enrollment.model.request.BulkActionRequest;
import com.springjwt.module.enrollment.model.request.CreateEnrollmentRequest;
import com.springjwt.module.enrollment.model.request.EnrollmentListRequest;
import com.springjwt.module.enrollment.model.request.RejectEnrollmentRequest;
import com.springjwt.module.enrollment.model.request.UpdatePaymentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Enrollment Management", description = "Admin review queue for student enrollments")
@SecurityRequirement(name = "Bearer Authentication")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/enrollments")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List enrollments")
    public ResponseEntity<PagedResult<EnrollmentDto>> listEnrollments(@Valid EnrollmentListRequest request) {
        Page<EnrollmentDto> data = enrollmentService.listEnrollments(request);
        return ResponseFactory.pagedResponse(
                data.getContent(), request.getPage(), request.getSize(), data.getTotalElements());
    }

    @GetMapping("/enrollments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get enrollment details")
    public ResponseEntity<ApiResult<EnrollmentDto>> getEnrollment(@PathVariable Long id) {
        return ResponseFactory.success(enrollmentService.getEnrollmentById(id));
    }

    @PostMapping("/enrollments")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create enrollment manually (admin)")
    public ResponseEntity<ApiResult<EnrollmentDto>> createEnrollment(
            @Valid @RequestBody CreateEnrollmentRequest request) {
        EnrollmentDto created = enrollmentService.createEnrollment(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, "/api/enrollments/" + created.getId());
        return new ResponseEntity<>(ApiResult.success(created), headers, HttpStatus.CREATED);
    }

    @PatchMapping("/enrollments/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve enrollment and assign to a class")
    public ResponseEntity<ApiResult<EnrollmentDto>> approveEnrollment(
            @PathVariable Long id,
            @Valid @RequestBody ApproveEnrollmentRequest request) {
        return ResponseFactory.success(enrollmentService.approveEnrollment(id, request));
    }

    @PatchMapping("/enrollments/{id}/waitlist")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Move enrollment to waitlist")
    public ResponseEntity<ApiResult<EnrollmentDto>> waitlistEnrollment(@PathVariable Long id) {
        return ResponseFactory.success(enrollmentService.waitlistEnrollment(id));
    }

    @PatchMapping("/enrollments/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject enrollment with reason")
    public ResponseEntity<ApiResult<EnrollmentDto>> rejectEnrollment(
            @PathVariable Long id,
            @Valid @RequestBody RejectEnrollmentRequest request) {
        return ResponseFactory.success(enrollmentService.rejectEnrollment(id, request));
    }

    @PatchMapping("/enrollments/{id}/payment")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update enrollment payment status / amount")
    public ResponseEntity<ApiResult<EnrollmentDto>> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentRequest request) {
        return ResponseFactory.success(enrollmentService.updatePayment(id, request));
    }

    @GetMapping("/enrollments/status-tabs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Count enrollments by status")
    public ResponseEntity<ApiResult<EnrollmentStatusTabsDto>> getStatusTabs() {
        return ResponseFactory.success(enrollmentService.getStatusTabs());
    }

    @PatchMapping("/enrollments/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bulk approve / waitlist enrollments")
    public ResponseEntity<ApiResult<BulkActionResultDto>> bulkAction(
            @Valid @RequestBody BulkActionRequest request) {
        return ResponseFactory.success(enrollmentService.bulkAction(request));
    }
}
