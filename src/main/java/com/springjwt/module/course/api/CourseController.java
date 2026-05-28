package com.springjwt.module.course.api;

import com.springjwt.common.base.response.ApiResult;
import com.springjwt.common.base.response.PagedResult;
import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.module.course.business.CourseService;
import com.springjwt.module.course.model.dto.CourseDto;
import com.springjwt.module.course.model.dto.CourseToolTabDto;
import com.springjwt.module.course.model.dto.CourseStatsDto;
import com.springjwt.module.course.model.dto.PublicCourseDetailDto;
import com.springjwt.module.course.model.dto.PublicCourseDto;
import com.springjwt.module.course.model.request.*;
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
@Tag(name = "Course Management", description = "Course catalog APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/courses")
    @Operation(summary = "List courses")
    public ResponseEntity<PagedResult<CourseDto>> listCourses(@Valid CourseListRequest request) {
        Page<CourseDto> data = courseService.listCourses(request);
        return ResponseFactory.pagedResponse(
                data.getContent(), request.getPage(), request.getSize(), data.getTotalElements());
    }

    @GetMapping("/courses/{id}")
    @Operation(summary = "Get course details")
    public ResponseEntity<ApiResult<CourseDto>> getCourse(@PathVariable Long id) {
        return ResponseFactory.success(courseService.getCourseById(id));
    }

    @PostMapping("/courses")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create course")
    public ResponseEntity<ApiResult<CourseDto>> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        CourseDto course = courseService.createCourse(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, "/api/courses/" + course.getId());
        return new ResponseEntity<>(ApiResult.success(course), headers, HttpStatus.CREATED);
    }

    @PutMapping("/courses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update course")
    public ResponseEntity<ApiResult<CourseDto>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request) {
        return ResponseFactory.success(courseService.updateCourse(id, request));
    }

    @PatchMapping("/courses/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update course status")
    public ResponseEntity<ApiResult<CourseDto>> updateCourseStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseStatusRequest request) {
        return ResponseFactory.success(courseService.updateCourseStatus(id, request));
    }

    @PostMapping("/courses/{id}/duplicate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Duplicate course")
    public ResponseEntity<ApiResult<CourseDto>> duplicateCourse(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) DuplicateCourseRequest request) {
        DuplicateCourseRequest payload = request == null ? DuplicateCourseRequest.builder().build() : request;
        CourseDto course = courseService.duplicateCourse(id, payload);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, "/api/courses/" + course.getId());
        return new ResponseEntity<>(ApiResult.success(course), headers, HttpStatus.CREATED);
    }

    @DeleteMapping("/courses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete course")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/tool-tabs")
    @Operation(summary = "Count courses by tool (with 'all' tab)")
    public ResponseEntity<ApiResult<List<CourseToolTabDto>>> getToolTabs() {
        return ResponseFactory.success(courseService.getToolTabs());
    }

    @GetMapping("/courses/stats")
    @Operation(summary = "Course totals (total / published / drafts)")
    public ResponseEntity<ApiResult<CourseStatsDto>> getStats() {
        return ResponseFactory.success(courseService.getStats());
    }

    /**
     * Public, unauthenticated list of newest published courses for the marketing landing page.
     * Whitelisted in {@link com.springjwt.core.security.WebSecurityConfig} under {@code /api/public/**}.
     */
    @GetMapping("/public/courses")
    @Operation(summary = "Public landing-page courses (no auth)",
            description = "Returns newest published courses for the marketing site. Defaults to 4 items.")
    public ResponseEntity<ApiResult<List<PublicCourseDto>>> listPublicCourses(
            @RequestParam(value = "limit", defaultValue = "4") int limit) {
        return ResponseFactory.success(courseService.listPublicCourses(limit));
    }

    @GetMapping("/public/courses/{id}")
    @Operation(summary = "Public course detail (no auth)",
            description = "Full course payload including ordered sessions for the marketing detail sheet. "
                    + "Returns 404 if the course is not published.")
    public ResponseEntity<ApiResult<PublicCourseDetailDto>> getPublicCourse(@PathVariable Long id) {
        return ResponseFactory.success(courseService.getPublicCourseById(id));
    }
}
