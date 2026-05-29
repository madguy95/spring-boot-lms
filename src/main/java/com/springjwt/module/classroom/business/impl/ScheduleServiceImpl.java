package com.springjwt.module.classroom.business.impl;

import com.springjwt.module.classroom.business.ScheduleService;
import com.springjwt.module.classroom.domain.entity.ClassDaySchedule;
import com.springjwt.module.classroom.domain.entity.ClassEntity;
import com.springjwt.module.classroom.domain.repository.ClassRepository;
import com.springjwt.module.classroom.model.dto.ScheduleDayDto;
import com.springjwt.module.classroom.model.dto.ScheduleEventDto;
import com.springjwt.module.classroom.model.dto.ScheduleFiltersDto;
import com.springjwt.module.classroom.model.dto.ScheduleMonthCellDto;
import com.springjwt.module.classroom.model.dto.ScheduleMonthDto;
import com.springjwt.module.classroom.model.dto.ScheduleMonthWeekDto;
import com.springjwt.module.classroom.model.dto.ScheduleOptionDto;
import com.springjwt.module.classroom.model.dto.ScheduleSummaryDto;
import com.springjwt.module.classroom.model.dto.ScheduleWeekDto;
import com.springjwt.module.classroom.model.request.ScheduleListRequest;
import com.springjwt.module.course.domain.entity.Course;
import com.springjwt.module.teacher.domain.entity.Teacher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    // The FE grid is anchored at 08:00 — startOffsetMin is measured from this.
    // Sessions outside 08:00..18:00 can still be returned; the FE clips visually.
    private static final int GRID_BASELINE_MIN = 8 * 60;

    // FE-friendly day key (Mon..Sun) matching ClassDaySchedule.day storage.
    private static final List<String> DAY_KEYS =
            List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

    // Cap on events embedded inside a month cell — beyond this the FE shows a
    // "+N more" affordance and the user drills into day view for the full list.
    private static final int MONTH_CELL_EVENT_CAP = 3;

    // Map course `tool` master codes to the FE category that drives the colour
    // legend. Unknown tools fall through to "scratch" — safe neutral colour.
    private static final Map<String, String> TOOL_TO_CATEGORY = Map.of(
            "scratch", "scratch",
            "python", "python",
            "web", "web",
            "robotics", "robotics",
            "arduino", "robotics",
            "game", "game_ai",
            "ai", "game_ai"
    );

    private final ClassRepository classRepository;

    @Override
    @Transactional(readOnly = true)
    public ScheduleWeekDto getWeek(ScheduleListRequest request) {
        LocalDate today = LocalDate.now();
        boolean dayView = "day".equalsIgnoreCase(request.getView());

        LocalDate windowStart;
        LocalDate windowEnd;
        if (dayView) {
            // Day view: anchor IS the date (no Monday snap). Window is a single day.
            windowStart = request.getAnchor() != null ? request.getAnchor() : today;
            windowEnd = windowStart;
        } else {
            windowStart = resolveMonday(request.getAnchor(), today);
            windowEnd = windowStart.plusDays(6);
        }

        List<ClassEntity> classes = classRepository.findActiveInWeek(
                windowStart, windowEnd,
                request.getTeacherId(),
                request.getClassId(),
                normalize(request.getLocation())
        );

        List<ScheduleEventDto> events = expandEventsForWindow(classes, windowStart, windowEnd, dayView);
        List<ScheduleDayDto> days = buildDayHeaders(windowStart, windowEnd, today);
        ScheduleSummaryDto summary = buildSummary(windowStart, windowEnd, today, events, classes, dayView);

        return ScheduleWeekDto.builder()
                .summary(summary)
                .days(days)
                .events(events)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleMonthDto getMonth(ScheduleListRequest request) {
        LocalDate today = LocalDate.now();
        LocalDate anchor = request.getAnchor() != null ? request.getAnchor() : today;
        LocalDate firstOfMonth = anchor.withDayOfMonth(1);
        LocalDate lastOfMonth = firstOfMonth.plusMonths(1).minusDays(1);

        // Grid window = Monday of the week containing the 1st .. Sunday of the
        // week containing the last day. Captures leading/trailing days from
        // adjacent months so the grid is always full rows of 7.
        LocalDate gridStart = mondayOf(firstOfMonth);
        LocalDate gridEnd = sundayOf(lastOfMonth);

        List<ClassEntity> classes = classRepository.findActiveInWeek(
                gridStart, gridEnd,
                request.getTeacherId(),
                request.getClassId(),
                normalize(request.getLocation())
        );

        // Group events by date for fast cell lookup.
        Map<LocalDate, List<ScheduleEventDto>> byDate = new LinkedHashMap<>();
        Set<String> distinctTeachers = new HashSet<>();
        int totalStudentMinutes = 0;
        int totalSessions = 0;
        for (ClassEntity c : classes) {
            for (ClassDaySchedule day : c.getDaySchedules()) {
                int dayIndex = DAY_KEYS.indexOf(day.getDay());
                if (dayIndex < 0) continue;
                int startMin = toMinutes(day.getStartTime());
                int endMin = toMinutes(day.getEndTime());
                int duration = Math.max(endMin - startMin, 0);
                if (duration == 0) continue;

                // Iterate over every grid date and emit an event whenever the
                // weekday + class term align.
                for (LocalDate d = gridStart; !d.isAfter(gridEnd); d = d.plusDays(1)) {
                    if (((d.getDayOfWeek().getValue() + 6) % 7) != dayIndex) continue;
                    if (d.isBefore(c.getStartDate()) || d.isAfter(c.getEndDate())) continue;
                    // Only count toward "total student-hours" when the date is
                    // actually in this month — leading/trailing belong to neighbours.
                    boolean inMonth = !d.isBefore(firstOfMonth) && !d.isAfter(lastOfMonth);

                    ScheduleEventDto evt = ScheduleEventDto.builder()
                            .id("se-" + c.getId() + "-" + day.getPosition() + "-" + d)
                            .classId(String.valueOf(c.getId()))
                            .teacherId(c.getTeacher() != null ? String.valueOf(c.getTeacher().getId()) : null)
                            .dayIndex(dayIndex)
                            .startOffsetMin(startMin - GRID_BASELINE_MIN)
                            .durationMin(duration)
                            .title(c.getName())
                            .detail(buildDetail(c))
                            .timeLabel(day.getStartTime() + " — " + day.getEndTime())
                            .location(locationLabel(c))
                            .category(toCategory(c.getCourse()))
                            .build();
                    byDate.computeIfAbsent(d, k -> new ArrayList<>()).add(evt);

                    if (inMonth) {
                        if (evt.getTeacherId() != null) distinctTeachers.add(evt.getTeacherId());
                        int enrolled = c.getEnrolled() == null ? 0 : c.getEnrolled();
                        totalStudentMinutes += enrolled * duration;
                        totalSessions++;
                    }
                }
            }
        }

        List<ScheduleMonthWeekDto> weeks = new ArrayList<>();
        List<ScheduleMonthCellDto> currentRow = new ArrayList<>(7);
        for (LocalDate d = gridStart; !d.isAfter(gridEnd); d = d.plusDays(1)) {
            List<ScheduleEventDto> all = byDate.getOrDefault(d, List.of());
            List<ScheduleEventDto> preview = all.size() <= MONTH_CELL_EVENT_CAP
                    ? all
                    : new ArrayList<>(all.subList(0, MONTH_CELL_EVENT_CAP));

            boolean inMonth = !d.isBefore(firstOfMonth) && !d.isAfter(lastOfMonth);
            currentRow.add(ScheduleMonthCellDto.builder()
                    .isoDate(d)
                    .date(d.getDayOfMonth())
                    .inMonth(inMonth)
                    .isToday(d.equals(today) ? Boolean.TRUE : null)
                    .eventCount(all.size())
                    .events(preview)
                    .build());
            if (currentRow.size() == 7) {
                weeks.add(ScheduleMonthWeekDto.builder().cells(currentRow).build());
                currentRow = new ArrayList<>(7);
            }
        }

        String weekLabel = firstOfMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " " + firstOfMonth.getYear();
        ScheduleSummaryDto summary = ScheduleSummaryDto.builder()
                .weekLabel(weekLabel)
                .monthLabel(weekLabel)
                .todayLabel(monthHasToday(firstOfMonth, lastOfMonth, today) ? "Today" : null)
                .weekStart(firstOfMonth)
                .weekEnd(lastOfMonth)
                .totalSessions(totalSessions)
                .totalTeachers(distinctTeachers.size())
                .totalStudentHours((int) Math.round(totalStudentMinutes / 60.0))
                .build();

        return ScheduleMonthDto.builder()
                .summary(summary)
                .weekdayHeaders(DAY_KEYS)
                .weeks(weeks)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleFiltersDto getFilters() {
        List<ClassEntity> schedulable = classRepository.findSchedulableClasses();

        Map<Long, ScheduleOptionDto> teachers = new LinkedHashMap<>();
        Map<Long, ScheduleOptionDto> classes = new LinkedHashMap<>();
        Map<String, ScheduleOptionDto> locations = new LinkedHashMap<>();

        for (ClassEntity c : schedulable) {
            Teacher t = c.getTeacher();
            if (t != null) {
                teachers.computeIfAbsent(t.getId(), id -> ScheduleOptionDto.builder()
                        .id(String.valueOf(id))
                        .label(teacherDisplayName(t))
                        .build());
            }
            classes.computeIfAbsent(c.getId(), id -> ScheduleOptionDto.builder()
                    .id(String.valueOf(id))
                    .label(c.getName())
                    .build());
            addLocation(locations, c.getLocation());
            addLocation(locations, c.getRoom());
        }

        return ScheduleFiltersDto.builder()
                .teachers(new ArrayList<>(teachers.values()))
                .classes(new ArrayList<>(classes.values()))
                .locations(new ArrayList<>(locations.values()))
                .build();
    }

    // ----- helpers -----

    private LocalDate resolveMonday(LocalDate requested, LocalDate today) {
        LocalDate anchor = requested != null ? requested : today;
        return mondayOf(anchor);
    }

    private LocalDate mondayOf(LocalDate date) {
        int offset = date.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        return date.minusDays(offset);
    }

    private LocalDate sundayOf(LocalDate date) {
        int offset = DayOfWeek.SUNDAY.getValue() - date.getDayOfWeek().getValue();
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) return date;
        return date.plusDays(offset == 0 ? 0 : offset);
    }

    private boolean monthHasToday(LocalDate first, LocalDate last, LocalDate today) {
        return !today.isBefore(first) && !today.isAfter(last);
    }

    // Walks every date in [windowStart, windowEnd] and emits one event row per
    // matching (class, daySchedule) — works for both single-day and full-week
    // windows. dayIndex is relative to windowStart so day-view events sit in
    // column 0 of the FE renderer.
    private List<ScheduleEventDto> expandEventsForWindow(List<ClassEntity> classes,
                                                         LocalDate windowStart,
                                                         LocalDate windowEnd,
                                                         boolean dayView) {
        List<ScheduleEventDto> out = new ArrayList<>();
        for (ClassEntity c : classes) {
            for (ClassDaySchedule day : c.getDaySchedules()) {
                int weekday = DAY_KEYS.indexOf(day.getDay());
                if (weekday < 0) continue;

                int startMin = toMinutes(day.getStartTime());
                int endMin = toMinutes(day.getEndTime());
                int duration = Math.max(endMin - startMin, 0);
                if (duration == 0) continue;

                for (LocalDate d = windowStart; !d.isAfter(windowEnd); d = d.plusDays(1)) {
                    if (((d.getDayOfWeek().getValue() + 6) % 7) != weekday) continue;
                    if (d.isBefore(c.getStartDate()) || d.isAfter(c.getEndDate())) continue;

                    int dayIndex = dayView ? 0 : (int) java.time.temporal.ChronoUnit.DAYS.between(windowStart, d);
                    out.add(ScheduleEventDto.builder()
                            .id("se-" + c.getId() + "-" + day.getPosition() + "-" + d)
                            .classId(String.valueOf(c.getId()))
                            .teacherId(c.getTeacher() != null ? String.valueOf(c.getTeacher().getId()) : null)
                            .dayIndex(dayIndex)
                            .startOffsetMin(startMin - GRID_BASELINE_MIN)
                            .durationMin(duration)
                            .title(c.getName())
                            .detail(buildDetail(c))
                            .timeLabel(day.getStartTime() + " — " + day.getEndTime())
                            .location(locationLabel(c))
                            .category(toCategory(c.getCourse()))
                            .build());
                }
            }
        }
        return out;
    }

    private List<ScheduleDayDto> buildDayHeaders(LocalDate windowStart, LocalDate windowEnd, LocalDate today) {
        List<ScheduleDayDto> days = new ArrayList<>();
        for (LocalDate d = windowStart; !d.isAfter(windowEnd); d = d.plusDays(1)) {
            int dayKeyIdx = (d.getDayOfWeek().getValue() + 6) % 7;
            days.add(ScheduleDayDto.builder()
                    .shortName(DAY_KEYS.get(dayKeyIdx))
                    .date(d.getDayOfMonth())
                    .isToday(d.equals(today) ? Boolean.TRUE : null)
                    .isoDate(d)
                    .build());
        }
        return days;
    }

    private ScheduleSummaryDto buildSummary(LocalDate windowStart, LocalDate windowEnd, LocalDate today,
                                            List<ScheduleEventDto> events, List<ClassEntity> classes, boolean dayView) {
        Set<String> teachersInWindow = new HashSet<>();
        int studentMinutes = 0;
        for (ScheduleEventDto e : events) {
            if (e.getTeacherId() != null) teachersInWindow.add(e.getTeacherId());
            ClassEntity owner = findById(classes, e.getClassId());
            int enrolled = owner != null && owner.getEnrolled() != null ? owner.getEnrolled() : 0;
            studentMinutes += enrolled * e.getDurationMin();
        }

        String weekLabel = dayView
                ? windowStart.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                        + " " + formatMonthDay(windowStart)
                : "Week " + formatMonthDay(windowStart) + " — " + windowEnd.getDayOfMonth();
        String monthLabel = windowStart.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                + " " + windowStart.getYear();
        String todayLabel = null;
        if (!today.isBefore(windowStart) && !today.isAfter(windowEnd)) {
            todayLabel = today.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " (today)";
        }

        return ScheduleSummaryDto.builder()
                .weekLabel(weekLabel)
                .monthLabel(monthLabel)
                .todayLabel(todayLabel)
                .weekStart(windowStart)
                .weekEnd(windowEnd)
                .totalSessions(events.size())
                .totalTeachers(teachersInWindow.size())
                .totalStudentHours((int) Math.round(studentMinutes / 60.0))
                .build();
    }

    private ClassEntity findById(List<ClassEntity> classes, String id) {
        if (id == null) return null;
        for (ClassEntity c : classes) {
            if (Objects.equals(String.valueOf(c.getId()), id)) return c;
        }
        return null;
    }

    private String teacherDisplayName(Teacher t) {
        String first = t.getFirstName() == null ? "" : t.getFirstName().trim();
        String last = t.getLastName() == null ? "" : t.getLastName().trim();
        String full = (first + " " + last).trim();
        return full.isEmpty() ? ("Teacher #" + t.getId()) : full;
    }

    private String buildDetail(ClassEntity c) {
        StringBuilder sb = new StringBuilder();
        Teacher t = c.getTeacher();
        if (t != null) sb.append(shortTeacherName(t));
        String loc = locationLabel(c);
        if (loc != null && !loc.isBlank()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(loc);
        }
        return sb.toString();
    }

    private String shortTeacherName(Teacher t) {
        String first = t.getFirstName() == null ? "" : t.getFirstName().trim();
        String last = t.getLastName() == null ? "" : t.getLastName().trim();
        if (last.isEmpty()) return first;
        if (first.isEmpty()) return last;
        return first + " " + last.charAt(0) + ".";
    }

    private String locationLabel(ClassEntity c) {
        if (c.getRoom() != null && !c.getRoom().isBlank()) return c.getRoom();
        return c.getLocation();
    }

    private String toCategory(Course course) {
        if (course == null || course.getTool() == null) return "scratch";
        return TOOL_TO_CATEGORY.getOrDefault(course.getTool().toLowerCase(Locale.ROOT), "scratch");
    }

    private void addLocation(Map<String, ScheduleOptionDto> sink, String value) {
        if (value == null) return;
        String key = value.trim();
        if (key.isEmpty()) return;
        sink.computeIfAbsent(key, k -> ScheduleOptionDto.builder().id(k).label(k).build());
    }

    private int toMinutes(String hhmm) {
        if (hhmm == null) return 0;
        String[] parts = hhmm.split(":");
        if (parts.length < 2) return 0;
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }

    private String formatMonthDay(LocalDate date) {
        Month month = date.getMonth();
        return month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + date.getDayOfMonth();
    }
}
