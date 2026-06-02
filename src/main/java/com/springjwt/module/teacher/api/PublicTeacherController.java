package com.springjwt.module.teacher.api;

import com.springjwt.common.base.response.ApiResult;
import com.springjwt.common.base.response.ResponseFactory;
import com.springjwt.module.teacher.business.TeacherService;
import com.springjwt.module.teacher.model.dto.PublicTeacherDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Tag(name = "Public", description = "Public APIs — no authentication required")
public class PublicTeacherController {

    private final TeacherService teacherService;

    @GetMapping("/teachers")
    @Operation(summary = "List active teachers for public about page")
    public ResponseEntity<ApiResult<List<PublicTeacherDto>>> getPublicTeachers() {
        return ResponseFactory.success(teacherService.getPublicTeachers());
    }
}
