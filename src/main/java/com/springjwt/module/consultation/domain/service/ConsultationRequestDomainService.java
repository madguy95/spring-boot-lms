package com.springjwt.module.consultation.domain.service;

import com.springjwt.common.exception.AppException;
import com.springjwt.module.consultation.domain.entity.ConsultationRequest;
import com.springjwt.module.consultation.domain.repository.ConsultationRequestRepository;
import com.springjwt.module.course.domain.entity.Course;
import com.springjwt.module.course.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultationRequestDomainService {

    private final ConsultationRequestRepository consultationRequestRepository;
    private final CourseRepository courseRepository;

    public ConsultationRequest getByIdOrThrow(Long id) {
        return consultationRequestRepository.findById(id)
                .orElseThrow(() -> new AppException("consultation.notFound", HttpStatus.NOT_FOUND));
    }

    public Course getCourseOrNull(Long courseId) {
        if (courseId == null) return null;
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException("consultation.course.notFound", HttpStatus.BAD_REQUEST));
    }
}
