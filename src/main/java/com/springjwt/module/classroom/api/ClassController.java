package com.springjwt.module.classroom.api;

import com.springjwt.common.base.response.ApiResult;
import com.springjwt.common.base.response.PagedResult;
import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.module.classroom.business.ClassService;
import com.springjwt.module.classroom.model.dto.ClassDto;
import com.springjwt.module.classroom.model.dto.ClassStatsDto;
import com.springjwt.module.classroom.model.dto.ClassStatusTabsDto;
import com.springjwt.module.classroom.model.dto.ClassStudentDto;
import com.springjwt.module.classroom.model.request.ClassListRequest;
import com.springjwt.module.classroom.model.request.CreateClassRequest;
import com.springjwt.module.classroom.model.request.LifecycleActionRequest;
import com.springjwt.module.classroom.model.request.UpdateClassRequest;
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

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Class Management", description = "Class scheduling APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class ClassController {

    private final ClassService classService;

    @GetMapping("/classes")
    @Operation(summary = "List classes")
    public ResponseEntity<PagedResult<ClassDto>> listClasses(@Valid ClassListRequest request) {
        Page<ClassDto> data = classService.listClasses(request);
        return ResponseFactory.pagedResponse(
                data.getContent(), request.getPage(), request.getSize(), data.getTotalElements());
    }

    @GetMapping("/classes/{id}")
    @Operation(summary = "Get class details")
    public ResponseEntity<ApiResult<ClassDto>> getClass(@PathVariable Long id) {
        return ResponseFactory.success(classService.getClassById(id));
    }

    @PostMapping("/classes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create class")
    public ResponseEntity<ApiResult<ClassDto>> createClass(@Valid @RequestBody CreateClassRequest request) {
        ClassDto created = classService.createClass(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, "/api/classes/" + created.getId());
        return new ResponseEntity<>(ApiResult.success(created), headers, HttpStatus.CREATED);
    }

    @PutMapping("/classes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update class")
    public ResponseEntity<ApiResult<ClassDto>> updateClass(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClassRequest request) {
        return ResponseFactory.success(classService.updateClass(id, request));
    }

    @PatchMapping("/classes/{id}/lifecycle")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Apply lifecycle action (publish / unpublish / cancel)")
    public ResponseEntity<ApiResult<ClassDto>> applyLifecycleAction(
            @PathVariable Long id,
            @Valid @RequestBody LifecycleActionRequest request) {
        return ResponseFactory.success(classService.applyLifecycleAction(id, request));
    }

    @GetMapping("/classes/{id}/students")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "List students enrolled (active) in a class")
    public ResponseEntity<ApiResult<List<ClassStudentDto>>> getClassStudents(@PathVariable Long id) {
        return ResponseFactory.success(classService.getClassStudents(id));
    }

    @GetMapping("/classes/status-tabs")
    @Operation(summary = "Count classes by status (running / upcoming / ended + all)")
    public ResponseEntity<ApiResult<ClassStatusTabsDto>> getStatusTabs() {
        return ResponseFactory.success(classService.getStatusTabs());
    }

    @GetMapping("/classes/stats")
    @Operation(summary = "Class totals (total / running / upcoming / ended)")
    public ResponseEntity<ApiResult<ClassStatsDto>> getStats() {
        return ResponseFactory.success(classService.getStats());
    }
}
