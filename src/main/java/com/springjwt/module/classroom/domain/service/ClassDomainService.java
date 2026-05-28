package com.springjwt.module.classroom.domain.service;

import com.springjwt.common.exception.AppException;
import com.springjwt.module.classroom.domain.entity.ClassEntity;
import com.springjwt.module.classroom.domain.repository.ClassRepository;
import com.springjwt.module.course.domain.entity.Course;
import com.springjwt.module.course.domain.repository.CourseRepository;
import com.springjwt.module.teacher.domain.entity.Teacher;
import com.springjwt.module.teacher.domain.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClassDomainService {

    private static final String COURSE_PUBLISHED = "published";
    private static final String TEACHER_ACTIVE = "active";

    private final ClassRepository classRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;

    public ClassEntity getClassById(Long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new AppException("class.not.found", HttpStatus.NOT_FOUND));
    }

    // Only published courses can back a class — drafts shouldn't be schedulable.
    public Course getPublishedCourseOrThrow(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException("class.course.not.found", HttpStatus.BAD_REQUEST));
        if (!COURSE_PUBLISHED.equals(course.getStatus())) {
            throw new AppException("class.course.not.published", HttpStatus.BAD_REQUEST);
        }
        return course;
    }

    // Only active teachers can be assigned to a class — pending/on-leave should
    // be blocked at the API edge to keep stale rosters out of the schedule.
    public Teacher getActiveTeacherOrThrow(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new AppException("class.teacher.not.found", HttpStatus.BAD_REQUEST));
        if (!TEACHER_ACTIVE.equals(teacher.getStatus())) {
            throw new AppException("class.teacher.not.active", HttpStatus.BAD_REQUEST);
        }
        return teacher;
    }
}
