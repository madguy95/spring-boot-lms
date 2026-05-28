package com.springjwt.module.teacher.domain.service;

import com.springjwt.common.exception.AppException;
import com.springjwt.module.teacher.domain.entity.Subject;
import com.springjwt.module.teacher.domain.entity.Teacher;
import com.springjwt.module.teacher.domain.repository.SubjectRepository;
import com.springjwt.module.teacher.domain.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TeacherDomainService {

    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;

    public Teacher getTeacherById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new AppException("teacher.not.found", HttpStatus.NOT_FOUND));
    }

    public List<Subject> getSubjectsOrThrow(Set<Long> subjectIds) {
        List<Subject> subjects = subjectRepository.findByIdIn(subjectIds);
        if (subjects.size() != subjectIds.size()) {
            throw new AppException("teacher.subject.not.found", HttpStatus.BAD_REQUEST);
        }
        return subjects;
    }

    public void validatePrimarySubject(Set<Long> subjectIds, Long primarySubjectId) {
        if (!subjectIds.contains(primarySubjectId)) {
            throw new AppException("teacher.primary.subject.invalid", HttpStatus.BAD_REQUEST);
        }
    }
}

