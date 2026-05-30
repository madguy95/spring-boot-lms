package com.springjwt.module.dashboard.api;

import com.springjwt.common.base.response.ApiResult;
import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.module.dashboard.business.AdminDashboardService;
import com.springjwt.module.dashboard.model.dto.DashboardSummaryDto;
import com.springjwt.module.dashboard.model.request.DashboardSummaryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Aggregated overview shown on the LMS admin landing page")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get the admin dashboard summary (KPIs + recent enrollments + upcoming classes + course fill)")
    public ResponseEntity<ApiResult<DashboardSummaryDto>> getSummary(DashboardSummaryRequest request) {
        return ResponseFactory.success(adminDashboardService.getSummary(request));
    }
}
