package com.springjwt.module.teacher.business;

import com.springjwt.module.teacher.model.dto.*;
import com.springjwt.module.teacher.model.request.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TeacherService {

    Page<TeacherDto> listTeachers(TeacherListRequest request);

    TeacherDto getTeacherById(Long id, Long currentUserId, boolean isAdmin);

    TeacherDto createTeacher(CreateTeacherRequest request);

    TeacherDto updateTeacher(Long id, UpdateTeacherRequest request);

    TeacherDto updateTeacherStatus(Long id, UpdateTeacherStatusRequest request);

    void deleteTeacher(Long id);

    TeacherStatusTabsDto getStatusTabs();

    List<TeacherOptionDto> getTeacherOptions(String status);

    List<SubjectDto> getSubjects();
}

