package com.springjwt.module.classroom.business;

import com.springjwt.module.classroom.model.dto.ClassDto;
import com.springjwt.module.classroom.model.dto.ClassStatsDto;
import com.springjwt.module.classroom.model.dto.ClassStatusTabsDto;
import com.springjwt.module.classroom.model.dto.ClassStudentDto;
import com.springjwt.module.classroom.model.request.ClassListRequest;
import com.springjwt.module.classroom.model.request.CreateClassRequest;
import com.springjwt.module.classroom.model.request.LifecycleActionRequest;
import com.springjwt.module.classroom.model.request.UpdateClassRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ClassService {

    Page<ClassDto> listClasses(ClassListRequest request);

    ClassDto getClassById(Long id);

    ClassDto createClass(CreateClassRequest request);

    ClassDto updateClass(Long id, UpdateClassRequest request);

    ClassDto applyLifecycleAction(Long id, LifecycleActionRequest request);

    ClassStatusTabsDto getStatusTabs();

    ClassStatsDto getStats();

    List<ClassStudentDto> getClassStudents(Long id);
}
