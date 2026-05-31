package com.springjwt.module.teacher.api;

import com.springjwt.common.base.response.ApiResult;
import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.module.auth.model.dto.UserPrincipal;
import com.springjwt.module.teacher.business.TeacherClassService;
import com.springjwt.module.teacher.model.dto.MyClassesSummaryDto;
import com.springjwt.module.teacher.model.request.MyClassesRequest;
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
@RequestMapping("/api/teacher/classes")
@RequiredArgsConstructor
@Tag(name = "Teacher Classes", description = "The signed-in teacher's own classes (My classes view)")
@SecurityRequirement(name = "Bearer Authentication")
public class TeacherClassController {

    private final TeacherClassService teacherClassService;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get the signed-in teacher's classes summary (stats + class cards)")
    public ResponseEntity<ApiResult<MyClassesSummaryDto>> getMyClasses(
            MyClassesRequest request, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseFactory.success(teacherClassService.getMyClasses(principal.getId(), request));
    }
}
