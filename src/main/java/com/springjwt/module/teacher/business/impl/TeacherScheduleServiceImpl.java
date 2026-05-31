package com.springjwt.module.teacher.business.impl;

import com.springjwt.common.exception.AppException;
import com.springjwt.module.classroom.domain.entity.ClassDaySchedule;
import com.springjwt.module.classroom.domain.entity.ClassEntity;
import com.springjwt.module.classroom.domain.repository.ClassRepository;
import com.springjwt.module.classroom.domain.service.ClassLifecyclePolicy;
import com.springjwt.module.teacher.business.TeacherScheduleService;
import com.springjwt.module.teacher.domain.entity.Teacher;
import com.springjwt.module.teacher.domain.repository.TeacherRepository;
import com.springjwt.module.teacher.model.dto.TeacherScheduleClassChipDto;
import com.springjwt.module.teacher.model.dto.TeacherScheduleDayDto;
import com.springjwt.module.teacher.model.dto.TeacherScheduleEventDto;
import com.springjwt.module.teacher.model.dto.TeacherScheduleSummaryDto;
import com.springjwt.module.teacher.model.dto.TeacherScheduleWeekDto;
import com.springjwt.module.teacher.model.request.TeacherScheduleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeacherScheduleServiceImpl implements TeacherScheduleService {

    // FE grid renders Monday-first; ClassDaySchedule.day stores "Mon".."Sun".
    private static final List<String> DAY_KEYS =
            List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

    private static final String ONLINE = "online";

    // Per-class swatch palette, hashed off the class id so a class keeps the same
    // colour across the chip and all its grid events.
    private static final List<String> CLASS_COLORS =
            List.of("emerald", "sky", "amber", "violet", "rose", "slate");

    private final TeacherRepository teacherRepository;
    private final ClassRepository classRepository;

    @Override
    @Transactional(readOnly = true)
    public TeacherScheduleWeekDto getWeek(Long userId, TeacherScheduleRequest request) {
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException("teacher.not.found", HttpStatus.NOT_FOUND));

        LocalDate today = LocalDate.now();
        LocalDate weekStart = mondayOf(request.getAnchor() != null ? request.getAnchor() : today);
        LocalDate weekEnd = weekStart.plusDays(6);

        // Pull every class the teacher runs that overlaps the week (classId filter
        // is applied later so the chips can still list all classes).
        List<ClassEntity> classes = classRepository
                .findActiveInWeek(weekStart, weekEnd, teacher.getId(), null, null)
                .stream()
                .filter(c -> ClassLifecyclePolicy.LIFECYCLE_PUBLISHED.equals(c.getLifecycleStatus()))
                .toList();

        List<TeacherScheduleClassChipDto> chips = buildChips(classes);
        List<TeacherScheduleEventDto> events =
                buildEvents(classes, weekStart, weekEnd, request.getClassId());
        List<TeacherScheduleDayDto> days = buildDays(weekStart, today);
        TeacherScheduleSummaryDto summary = buildSummary(weekStart, weekEnd, today, events);

        return TeacherScheduleWeekDto.builder()
                .summary(summary)
                .days(days)
                .events(events)
                .classChips(chips)
                .build();
    }

    // ----- builders -----

    private List<TeacherScheduleClassChipDto> buildChips(List<ClassEntity> classes) {
        Map<Long, TeacherScheduleClassChipDto> chips = new LinkedHashMap<>();
        for (ClassEntity c : classes) {
            chips.computeIfAbsent(c.getId(), id -> TeacherScheduleClassChipDto.builder()
                    .id(id)
                    .label(c.getLabel())
                    .color(pickColor(id))
                    .build());
        }
        return new ArrayList<>(chips.values());
    }

    private List<TeacherScheduleEventDto> buildEvents(List<ClassEntity> classes,
                                                      LocalDate weekStart,
                                                      LocalDate weekEnd,
                                                      Long classIdFilter) {
        List<TeacherScheduleEventDto> out = new ArrayList<>();
        for (ClassEntity c : classes) {
            if (classIdFilter != null && !classIdFilter.equals(c.getId())) continue;

            String color = pickColor(c.getId());
            boolean online = isOnline(c);
            int studentCount = c.getEnrolled() == null ? 0 : c.getEnrolled();
            String location = locationLabel(c);
            String courseCode = c.getCourse() != null ? c.getCourse().getCode() : "—";
            String title = c.getCourse() != null ? c.getCourse().getTitle() : c.getName();

            for (ClassDaySchedule day : c.getDaySchedules()) {
                int weekday = DAY_KEYS.indexOf(day.getDay());
                if (weekday < 0) continue;
                int startMin = toMinutes(day.getStartTime());
                int duration = Math.max(toMinutes(day.getEndTime()) - startMin, 0);
                if (duration == 0) continue;

                for (LocalDate d = weekStart; !d.isAfter(weekEnd); d = d.plusDays(1)) {
                    if (((d.getDayOfWeek().getValue() + 6) % 7) != weekday) continue;
                    if (d.isBefore(c.getStartDate()) || d.isAfter(c.getEndDate())) continue;

                    int dayIndex = (int) ChronoUnit.DAYS.between(weekStart, d);
                    out.add(TeacherScheduleEventDto.builder()
                            .id("tse-" + c.getId() + "-" + day.getPosition() + "-" + d)
                            .classId(c.getId())
                            .classLabel(c.getLabel())
                            .courseCode(courseCode)
                            .title(title)
                            .dayIndex(dayIndex)
                            .startMin(startMin)
                            .durationMin(duration)
                            .location(location)
                            .isOnline(online)
                            .studentCount(studentCount)
                            .color(color)
                            // Makeup sessions aren't modelled yet — every event is a
                            // regular cadence session for now.
                            .isMakeup(false)
                            .build());
                }
            }
        }
        return out;
    }

    private List<TeacherScheduleDayDto> buildDays(LocalDate weekStart, LocalDate today) {
        List<TeacherScheduleDayDto> days = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            LocalDate d = weekStart.plusDays(i);
            days.add(TeacherScheduleDayDto.builder()
                    .dayOfMonth(d.getDayOfMonth())
                    .isToday(d.equals(today) ? Boolean.TRUE : null)
                    .isoDate(d)
                    .build());
        }
        return days;
    }

    private TeacherScheduleSummaryDto buildSummary(LocalDate weekStart, LocalDate weekEnd,
                                                   LocalDate today, List<TeacherScheduleEventDto> events) {
        int teachingMinutes = 0;
        int onlineCount = 0;
        for (TeacherScheduleEventDto e : events) {
            teachingMinutes += e.getDurationMin();
            if (e.isOnline()) onlineCount++;
        }
        boolean weekHasToday = !today.isBefore(weekStart) && !today.isAfter(weekEnd);
        return TeacherScheduleSummaryDto.builder()
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .todayIso(weekHasToday ? today : null)
                .sessionsThisWeek(events.size())
                .teachingMinutes(teachingMinutes)
                .onlineCount(onlineCount)
                .makeupCount(0)
                .build();
    }

    // ----- helpers -----

    private LocalDate mondayOf(LocalDate date) {
        int offset = date.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        return date.minusDays(offset);
    }

    private String pickColor(Long id) {
        int idx = (int) Math.floorMod(id == null ? 0 : id, CLASS_COLORS.size());
        return CLASS_COLORS.get(idx);
    }

    private boolean isOnline(ClassEntity c) {
        return c.getLocation() != null && ONLINE.equalsIgnoreCase(c.getLocation().trim());
    }

    private String locationLabel(ClassEntity c) {
        if (c.getRoom() != null && !c.getRoom().isBlank()) return c.getRoom();
        return c.getLocation();
    }

    private int toMinutes(String hhmm) {
        if (hhmm == null) return 0;
        String[] parts = hhmm.split(":");
        if (parts.length < 2) return 0;
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
}
