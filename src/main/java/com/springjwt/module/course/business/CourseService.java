package com.springjwt.module.course.business;

import com.springjwt.module.course.model.dto.CourseDto;
import com.springjwt.module.course.model.dto.CourseStatsDto;
import com.springjwt.module.course.model.dto.CourseToolTabDto;
import com.springjwt.module.course.model.dto.PublicCourseDto;
import com.springjwt.module.course.model.request.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CourseService {

    Page<CourseDto> listCourses(CourseListRequest request);

    CourseDto getCourseById(Long id);

    CourseDto createCourse(CreateCourseRequest request);

    CourseDto updateCourse(Long id, UpdateCourseRequest request);

    CourseDto updateCourseStatus(Long id, UpdateCourseStatusRequest request);

    CourseDto duplicateCourse(Long id, DuplicateCourseRequest request);

    void deleteCourse(Long id);

    List<CourseToolTabDto> getToolTabs();

    CourseStatsDto getStats();

    /**
     * Public-facing list of newest published courses for the marketing landing page.
     * Never includes drafts or unpublished entries.
     */
    List<PublicCourseDto> listPublicCourses(int limit);

    /**
     * Public-facing detail for one course. Throws 404 if the course doesn't exist or isn't
     * currently published — drafts/unpublished are invisible to unauthenticated callers.
     */
    com.springjwt.module.course.model.dto.PublicCourseDetailDto getPublicCourseById(Long id);
}
