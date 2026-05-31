package com.springjwt.module.teacher.api;

import com.springjwt.common.base.response.ApiResult;
import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.module.auth.model.dto.UserPrincipal;
import com.springjwt.module.teacher.business.TeacherScheduleService;
import com.springjwt.module.teacher.model.dto.TeacherScheduleWeekDto;
import com.springjwt.module.teacher.model.request.TeacherScheduleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher/schedule")
@RequiredArgsConstructor
@Tag(name = "Teacher Schedule", description = "The signed-in teacher's weekly teaching calendar")
@SecurityRequirement(name = "Bearer Authentication")
public class TeacherScheduleController {

    private final TeacherScheduleService teacherScheduleService;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get the signed-in teacher's weekly schedule (grid events + stats + class chips)")
    public ResponseEntity<ApiResult<TeacherScheduleWeekDto>> getWeek(
            TeacherScheduleRequest request, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseFactory.success(teacherScheduleService.getWeek(principal.getId(), request));
    }
}
