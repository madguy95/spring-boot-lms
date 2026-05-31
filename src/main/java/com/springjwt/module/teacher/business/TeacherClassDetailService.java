package com.springjwt.module.teacher.business;

import com.springjwt.module.teacher.model.dto.TeacherClassDetailDto;
import com.springjwt.module.teacher.model.dto.TeacherClassSessionsDto;
import com.springjwt.module.teacher.model.dto.TeacherClassStudentsDto;
import com.springjwt.module.teacher.model.dto.TeacherSessionAttendanceDto;
import com.springjwt.module.teacher.model.dto.TeacherSessionNotesDto;
import com.springjwt.module.teacher.model.request.SaveSessionAttendanceRequest;
import com.springjwt.module.teacher.model.request.SaveSessionNotesRequest;

/**
 * Business operations for the Teacher "Class detail" screen. Every method is
 * scoped to the signed-in teacher (userId) and validates that the class belongs
 * to them before returning data.
 */
public interface TeacherClassDetailService {

    TeacherClassDetailDto getDetail(Long userId, Long classId);

    TeacherClassStudentsDto getStudents(Long userId, Long classId, String search);

    TeacherClassSessionsDto getSessions(Long userId, Long classId);

    TeacherSessionAttendanceDto getAttendance(Long userId, Long classId, String sessionCode);

    TeacherSessionAttendanceDto saveAttendance(Long userId, Long classId, String sessionCode,
                                               SaveSessionAttendanceRequest request);

    TeacherSessionNotesDto getNotes(Long userId, Long classId, String sessionCode);

    TeacherSessionNotesDto saveNotes(Long userId, Long classId, String sessionCode,
                                     SaveSessionNotesRequest request);
}
