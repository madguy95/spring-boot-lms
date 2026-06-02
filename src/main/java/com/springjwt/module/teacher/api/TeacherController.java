package com.springjwt.module.teacher.api;

import com.springjwt.common.base.response.ApiResult;
import com.springjwt.common.base.response.PagedResult;
import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.module.auth.model.dto.UserPrincipal;
import com.springjwt.module.teacher.business.TeacherService;
import com.springjwt.module.teacher.model.dto.*;
import com.springjwt.module.teacher.model.request.*;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Teacher Management", description = "Teacher module APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List teachers")
    public ResponseEntity<PagedResult<TeacherDto>> listTeachers(@Valid TeacherListRequest request) {
        Page<TeacherDto> data = teacherService.listTeachers(request);
        return ResponseFactory.pagedResponse(data.getContent(), request.getPage(), request.getSize(), data.getTotalElements());
    }

    @GetMapping("/teachers/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Get teacher details")
    public ResponseEntity<ApiResult<TeacherDto>> getTeacher(@PathVariable Long id, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        TeacherDto teacher = teacherService.getTeacherById(id, principal.getId(), isAdmin);
        return ResponseFactory.success(teacher);
    }

    @PostMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create teacher")
    public ResponseEntity<ApiResult<TeacherDto>> createTeacher(@Valid @RequestBody CreateTeacherRequest request) {
        TeacherDto teacher = teacherService.createTeacher(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, "/api/teachers/" + teacher.getId());
        return new ResponseEntity<>(ApiResult.success(teacher), headers, HttpStatus.CREATED);
    }

    @PutMapping("/teachers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update teacher")
    public ResponseEntity<ApiResult<TeacherDto>> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTeacherRequest request) {
        return ResponseFactory.success(teacherService.updateTeacher(id, request));
    }

    @PatchMapping("/teachers/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update teacher status")
    public ResponseEntity<ApiResult<TeacherDto>> updateTeacherStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTeacherStatusRequest request) {
        return ResponseFactory.success(teacherService.updateTeacherStatus(id, request));
    }

    @DeleteMapping("/teachers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete teacher")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/teachers/status-tabs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Count teachers by status")
    public ResponseEntity<ApiResult<TeacherStatusTabsDto>> getStatusTabs() {
        return ResponseFactory.success(teacherService.getStatusTabs());
    }

    @GetMapping("/teachers/options")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Teacher options for dropdown")
    public ResponseEntity<ApiResult<List<TeacherOptionDto>>> getTeacherOptions(
            @RequestParam(required = false) String status) {
        return ResponseFactory.success(teacherService.getTeacherOptions(status));
    }

    @GetMapping("/subjects")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all subjects")
    public ResponseEntity<ApiResult<List<SubjectDto>>> getSubjects() {
        return ResponseFactory.success(teacherService.getSubjects());
    }

    @PatchMapping("/teachers/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset teacher password to default")
    public ResponseEntity<ApiResult<String>> resetTeacherPassword(@PathVariable Long id) {
        String tempPassword = teacherService.resetPassword(id);
        return ResponseFactory.success(tempPassword);
    }
}
