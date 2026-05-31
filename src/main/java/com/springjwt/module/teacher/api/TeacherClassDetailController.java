package com.springjwt.module.teacher.api;

import com.springjwt.common.base.response.ApiResult;
import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.module.auth.model.dto.UserPrincipal;
import com.springjwt.module.teacher.business.TeacherClassDetailService;
import com.springjwt.module.teacher.model.dto.TeacherClassDetailDto;
import com.springjwt.module.teacher.model.dto.TeacherClassSessionsDto;
import com.springjwt.module.teacher.model.dto.TeacherClassStudentsDto;
import com.springjwt.module.teacher.model.dto.TeacherSessionAttendanceDto;
import com.springjwt.module.teacher.model.dto.TeacherSessionNotesDto;
import com.springjwt.module.teacher.model.request.SaveSessionAttendanceRequest;
import com.springjwt.module.teacher.model.request.SaveSessionNotesRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Teacher "Class detail" screen: header/overview, roster, sessions, attendance
 * and per-student review notes. All endpoints are scoped to the signed-in
 * teacher's own class.
 */
@RestController
@RequestMapping("/api/teacher/classes/{classId}")
@RequiredArgsConstructor
@Tag(name = "Teacher Class Detail", description = "Class detail screen for the signed-in teacher")
@SecurityRequirement(name = "Bearer Authentication")
public class TeacherClassDetailController {

    private final TeacherClassDetailService service;

    private Long currentUserId(Authentication authentication) {
        return ((UserPrincipal) authentication.getPrincipal()).getId();
    }

    @GetMapping("/detail")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Class header + overview stats")
    public ResponseEntity<ApiResult<TeacherClassDetailDto>> getDetail(
            @PathVariable Long classId, Authentication authentication) {
        return ResponseFactory.success(service.getDetail(currentUserId(authentication), classId));
    }

    @GetMapping("/students")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Class roster (optionally filtered by search)")
    public ResponseEntity<ApiResult<TeacherClassStudentsDto>> getStudents(
            @PathVariable Long classId,
            @RequestParam(value = "search", required = false) String search,
            Authentication authentication) {
        return ResponseFactory.success(service.getStudents(currentUserId(authentication), classId, search));
    }

    @GetMapping("/sessions")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Sessions of the class + current session")
    public ResponseEntity<ApiResult<TeacherClassSessionsDto>> getSessions(
            @PathVariable Long classId, Authentication authentication) {
        return ResponseFactory.success(service.getSessions(currentUserId(authentication), classId));
    }

    @GetMapping("/sessions/{sessionId}/attendance")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Attendance marks for a session")
    public ResponseEntity<ApiResult<TeacherSessionAttendanceDto>> getAttendance(
            @PathVariable Long classId, @PathVariable String sessionId, Authentication authentication) {
        return ResponseFactory.success(service.getAttendance(currentUserId(authentication), classId, sessionId));
    }

    @PostMapping("/sessions/{sessionId}/attendance")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Save attendance marks for a session")
    public ResponseEntity<ApiResult<TeacherSessionAttendanceDto>> saveAttendance(
            @PathVariable Long classId, @PathVariable String sessionId,
            @RequestBody SaveSessionAttendanceRequest request, Authentication authentication) {
        return ResponseFactory.success(
                service.saveAttendance(currentUserId(authentication), classId, sessionId, request));
    }

    @GetMapping("/sessions/{sessionId}/notes")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Per-student review notes + class summary for a session")
    public ResponseEntity<ApiResult<TeacherSessionNotesDto>> getNotes(
            @PathVariable Long classId, @PathVariable String sessionId, Authentication authentication) {
        return ResponseFactory.success(service.getNotes(currentUserId(authentication), classId, sessionId));
    }

    @PostMapping("/sessions/{sessionId}/notes")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Save per-student review notes + class summary for a session")
    public ResponseEntity<ApiResult<TeacherSessionNotesDto>> saveNotes(
            @PathVariable Long classId, @PathVariable String sessionId,
            @RequestBody SaveSessionNotesRequest request, Authentication authentication) {
        return ResponseFactory.success(
                service.saveNotes(currentUserId(authentication), classId, sessionId, request));
    }
}
