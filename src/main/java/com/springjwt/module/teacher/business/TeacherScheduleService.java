package com.springjwt.module.teacher.business;

import com.springjwt.module.teacher.model.dto.TeacherScheduleWeekDto;
import com.springjwt.module.teacher.model.request.TeacherScheduleRequest;

public interface TeacherScheduleService {

    /**
     * Build the weekly teaching schedule (grid events + day headers + week stats
     * + class chips) for the teacher owning {@code userId}.
     *
     * @param userId  the authenticated user's id (resolved to a Teacher row)
     * @param request anchor week + optional class filter
     */
    TeacherScheduleWeekDto getWeek(Long userId, TeacherScheduleRequest request);
}
