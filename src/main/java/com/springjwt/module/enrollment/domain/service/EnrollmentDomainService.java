package com.springjwt.module.enrollment.domain.service;

import com.springjwt.common.exception.AppException;
import com.springjwt.module.classroom.domain.entity.ClassEntity;
import com.springjwt.module.classroom.domain.repository.ClassRepository;
import com.springjwt.module.course.domain.entity.Course;
import com.springjwt.module.course.domain.repository.CourseRepository;
import com.springjwt.module.enrollment.domain.entity.Enrollment;
import com.springjwt.module.enrollment.domain.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnrollmentDomainService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final ClassRepository classRepository;

    public Enrollment getEnrollmentById(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new AppException("enrollment.not.found", HttpStatus.NOT_FOUND));
    }

    public Course getCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException("enrollment.course.not.found", HttpStatus.BAD_REQUEST));
    }

    // Assignment target must be a published class that still has at least one
    // open seat. Draft / unpublished / cancelled classes shouldn't accept new
    // approvals from the enrollments queue.
    public ClassEntity getAssignableClassOrThrow(Long classId) {
        ClassEntity cls = classRepository.findById(classId)
                .orElseThrow(() -> new AppException("enrollment.class.not.found", HttpStatus.BAD_REQUEST));
        if (!"published".equals(cls.getLifecycleStatus())) {
            throw new AppException("enrollment.class.not.published", HttpStatus.BAD_REQUEST);
        }
        Integer capacity = cls.getCapacity() == null ? 0 : cls.getCapacity();
        Integer enrolled = cls.getEnrolled() == null ? 0 : cls.getEnrolled();
        if (enrolled >= capacity) {
            throw new AppException("enrollment.class.full", HttpStatus.CONFLICT);
        }
        return cls;
    }
}
