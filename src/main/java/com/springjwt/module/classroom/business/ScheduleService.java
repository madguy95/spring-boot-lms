package com.springjwt.module.classroom.business;

import com.springjwt.module.classroom.model.dto.ScheduleFiltersDto;
import com.springjwt.module.classroom.model.dto.ScheduleMonthDto;
import com.springjwt.module.classroom.model.dto.ScheduleWeekDto;
import com.springjwt.module.classroom.model.request.ScheduleListRequest;

public interface ScheduleService {

    /**
     * Calendar grid payload for week or day view. Day view returns the same DTO
     * shape with a single-element {@code days} array and events scoped to that
     * date — lets the FE reuse the week renderer in a 1-column layout.
     */
    ScheduleWeekDto getWeek(ScheduleListRequest request);

    /** Month view: 7-column × 4–6 row grid with per-cell event previews. */
    ScheduleMonthDto getMonth(ScheduleListRequest request);

    /** Distinct teacher / class / location options for the filter chips. */
    ScheduleFiltersDto getFilters();
}
