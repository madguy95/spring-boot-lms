package com.springjwt.module.classroom.api;

import com.springjwt.common.base.response.ApiResult;
import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.module.classroom.business.ScheduleService;
import com.springjwt.module.classroom.model.dto.ScheduleFiltersDto;
import com.springjwt.module.classroom.model.dto.ScheduleMonthDto;
import com.springjwt.module.classroom.model.dto.ScheduleWeekDto;
import com.springjwt.module.classroom.model.request.ScheduleListRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Schedule is a derived view over ClassEntity + ClassDaySchedule, not its own
// business entity — that's why it ships in the classroom module alongside
// ClassController instead of a standalone module.
@RestController
@RequestMapping("/api/admin/schedule")
@RequiredArgsConstructor
@Tag(name = "Admin Schedule", description = "Calendar view derived from class schedules")
@SecurityRequirement(name = "Bearer Authentication")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get week or day schedule (view=week|day)")
    public ResponseEntity<ApiResult<ScheduleWeekDto>> getWeek(@Valid ScheduleListRequest request) {
        return ResponseFactory.success(scheduleService.getWeek(request));
    }

    @GetMapping("/month")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get monthly schedule grid")
    public ResponseEntity<ApiResult<ScheduleMonthDto>> getMonth(@Valid ScheduleListRequest request) {
        return ResponseFactory.success(scheduleService.getMonth(request));
    }

    @GetMapping("/filters")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get teacher / class / location options for the filter chips")
    public ResponseEntity<ApiResult<ScheduleFiltersDto>> getFilters() {
        return ResponseFactory.success(scheduleService.getFilters());
    }
}
