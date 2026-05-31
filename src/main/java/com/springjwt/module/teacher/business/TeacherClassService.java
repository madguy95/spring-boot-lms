package com.springjwt.module.teacher.business;

import com.springjwt.module.teacher.model.dto.MyClassesSummaryDto;
import com.springjwt.module.teacher.model.request.MyClassesRequest;

public interface TeacherClassService {

    /**
     * Build the "My classes" summary (global stats + filtered class cards) for
     * the teacher owning {@code userId}.
     *
     * @param userId  the authenticated user's id (resolved to a Teacher row)
     * @param request optional status filter
     */
    MyClassesSummaryDto getMyClasses(Long userId, MyClassesRequest request);
}
