package com.springjwt.module.course.domain.service;

import com.springjwt.common.exception.AppException;
import com.springjwt.module.course.domain.entity.Course;
import com.springjwt.module.course.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseDomainService {

    private final CourseRepository courseRepository;

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new AppException("course.not.found", HttpStatus.NOT_FOUND));
    }

    public void validateUniqueCode(String code, Long excludeId) {
        boolean exists = excludeId == null
                ? courseRepository.existsByCode(code)
                : courseRepository.existsByCodeAndIdNot(code, excludeId);
        if (exists) {
            throw new AppException("course.code.exists", HttpStatus.CONFLICT);
        }
    }

    public void validateAgeRange(Integer minAge, Integer maxAge) {
        if (minAge != null && maxAge != null && maxAge < minAge) {
            throw new AppException("course.validation.ageRange.invalid", HttpStatus.BAD_REQUEST);
        }
    }
}
