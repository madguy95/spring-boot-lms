package com.springjwt.module.consultation.api;

import com.springjwt.common.base.response.ApiResult;
import com.springjwt.common.base.response.PagedResult;
import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.module.consultation.business.ConsultationRequestService;
import com.springjwt.module.consultation.model.dto.ConsultationRequestDto;
import com.springjwt.module.consultation.model.dto.ConsultationStatusTabsDto;
import com.springjwt.module.consultation.model.request.ConsultationListRequest;
import com.springjwt.module.consultation.model.request.SubmitConsultationRequest;
import com.springjwt.module.consultation.model.request.UpdateConsultationStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Consultation Requests", description = "Admin CRM queue for parent consultation requests")
@SecurityRequirement(name = "Bearer Authentication")
public class ConsultationRequestController {

    private final ConsultationRequestService consultationRequestService;

    // ---- Admin endpoints ----

    @GetMapping("/admin/consultation-requests")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List consultation requests (paginated, filterable)")
    public ResponseEntity<PagedResult<ConsultationRequestDto>> list(@Valid ConsultationListRequest request) {
        Page<ConsultationRequestDto> data = consultationRequestService.listConsultationRequests(request);
        return ResponseFactory.pagedResponse(
                data.getContent(), request.getPage(), request.getSize(), data.getTotalElements());
    }

    @GetMapping("/admin/consultation-requests/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get a single consultation request")
    public ResponseEntity<ApiResult<ConsultationRequestDto>> getById(@PathVariable Long id) {
        return ResponseFactory.success(consultationRequestService.getById(id));
    }

    @PatchMapping("/admin/consultation-requests/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update consultation request status")
    public ResponseEntity<ApiResult<ConsultationRequestDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateConsultationStatusRequest request) {
        return ResponseFactory.success(consultationRequestService.updateStatus(id, request));
    }

    @DeleteMapping("/admin/consultation-requests/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a consultation request")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        consultationRequestService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/consultation-requests/status-tabs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Count consultation requests by status")
    public ResponseEntity<ApiResult<ConsultationStatusTabsDto>> getStatusTabs() {
        return ResponseFactory.success(consultationRequestService.getStatusTabs());
    }

    // ---- Public endpoint (no auth) — whitelisted under /api/public/** ----

    @PostMapping("/public/consultations")
    @Operation(summary = "Submit a consultation request (public, no auth required)")
    public ResponseEntity<ApiResult<ConsultationRequestDto>> submitPublic(
            @Valid @RequestBody SubmitConsultationRequest request) {
        ConsultationRequestDto created = consultationRequestService.submitPublic(request);
        return new ResponseEntity<>(ApiResult.success(created), HttpStatus.CREATED);
    }
}
