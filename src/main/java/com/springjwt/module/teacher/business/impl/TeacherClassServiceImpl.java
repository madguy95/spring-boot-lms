package com.springjwt.module.teacher.business.impl;

import com.springjwt.common.exception.AppException;
import com.springjwt.module.classroom.domain.entity.ClassDaySchedule;
import com.springjwt.module.classroom.domain.entity.ClassEntity;
import com.springjwt.module.classroom.domain.repository.ClassRepository;
import com.springjwt.module.classroom.domain.service.ClassLifecyclePolicy;
import com.springjwt.module.course.domain.entity.Course;
import com.springjwt.module.enrollment.domain.entity.Enrollment;
import com.springjwt.module.enrollment.domain.repository.EnrollmentRepository;
import com.springjwt.module.teacher.business.TeacherClassService;
import com.springjwt.module.teacher.domain.entity.Teacher;
import com.springjwt.module.teacher.domain.repository.TeacherRepository;
import com.springjwt.module.teacher.model.dto.MyClassDto;
import com.springjwt.module.teacher.model.dto.MyClassStudentPreviewDto;
import com.springjwt.module.teacher.model.dto.MyClassesClassesStatsDto;
import com.springjwt.module.teacher.model.dto.MyClassesStatsDto;
import com.springjwt.module.teacher.model.dto.MyClassesStudentsStatsDto;
import com.springjwt.module.teacher.model.dto.MyClassesSummaryDto;
import com.springjwt.module.teacher.model.request.MyClassesRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TeacherClassServiceImpl implements TeacherClassService {

    private static final String STATUS_RUNNING = "running";
    private static final String STATUS_UPCOMING = "upcoming";
    private static final String STATUS_ENDED = "ended";
    private static final String STATUS_ALL = "all";

    private static final int PREVIEW_LIMIT = 3;
    private static final String ONLINE = "online";

    private static final DateTimeFormatter ENDED_AT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ClassDaySchedule.day stores "Mon".."Sun" — map to java.time so we can count
    // how many scheduled sessions have already happened.
    private static final Map<String, DayOfWeek> DAY_KEY_TO_DOW = Map.of(
            "Mon", DayOfWeek.MONDAY,
            "Tue", DayOfWeek.TUESDAY,
            "Wed", DayOfWeek.WEDNESDAY,
            "Thu", DayOfWeek.THURSDAY,
            "Fri", DayOfWeek.FRIDAY,
            "Sat", DayOfWeek.SATURDAY,
            "Sun", DayOfWeek.SUNDAY
    );

    // Output order: running cards first, then upcoming, then ended.
    private static final Map<String, Integer> STATUS_ORDER = Map.of(
            STATUS_RUNNING, 0,
            STATUS_UPCOMING, 1,
            STATUS_ENDED, 2
    );

    private final TeacherRepository teacherRepository;
    private final ClassRepository classRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassLifecyclePolicy lifecyclePolicy;

    @Override
    @Transactional(readOnly = true)
    public MyClassesSummaryDto getMyClasses(Long userId, MyClassesRequest request) {
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException("teacher.not.found", HttpStatus.NOT_FOUND));

        LocalDate today = LocalDate.now();
        List<ClassEntity> entities = classRepository.findPublishedByTeacher(teacher.getId());

        // One batched roster fetch for every card's avatar stack.
        Map<Long, List<Enrollment>> rosterByClass = loadRoster(entities);

        // Build the full set first — stats are global (independent of the filter).
        List<MyClassDto> all = new ArrayList<>(entities.size());
        for (ClassEntity c : entities) {
            String feStatus = toFeStatus(lifecyclePolicy.deriveDisplayStatus(c, today));
            if (feStatus == null) continue; // defensive: only published reach here
            all.add(toDto(c, feStatus, today, rosterByClass.getOrDefault(c.getId(), List.of())));
        }

        MyClassesStatsDto stats = buildStats(all);

        String filter = normalizeStatus(request == null ? null : request.getStatus());
        List<MyClassDto> classes = all.stream()
                .filter(dto -> STATUS_ALL.equals(filter) || dto.getStatus().equals(filter))
                .sorted(Comparator.comparingInt(dto -> STATUS_ORDER.getOrDefault(dto.getStatus(), 99)))
                .toList();

        return MyClassesSummaryDto.builder()
                .stats(stats)
                .classes(classes)
                .build();
    }

    // ----- stats -----

    private MyClassesStatsDto buildStats(List<MyClassDto> all) {
        long running = 0;
        long upcoming = 0;
        long ended = 0;
        long studentTotal = 0;
        for (MyClassDto dto : all) {
            switch (dto.getStatus()) {
                case STATUS_RUNNING -> running++;
                case STATUS_UPCOMING -> upcoming++;
                case STATUS_ENDED -> ended++;
                default -> { /* no other statuses reach the card list */ }
            }
            // "Total students" is a roll-up across every class the teacher owns,
            // matching "total classes" so the two headline numbers share scope.
            studentTotal += dto.getStudentCount();
        }

        MyClassesClassesStatsDto classes = MyClassesClassesStatsDto.builder()
                .total(all.size())
                .running(running)
                .upcoming(upcoming)
                .ended(ended)
                .build();

        MyClassesStudentsStatsDto students = MyClassesStudentsStatsDto.builder()
                .total(studentTotal)
                .build();

        return MyClassesStatsDto.builder()
                .classes(classes)
                .students(students)
                .build();
    }

    // ----- mapping -----

    private MyClassDto toDto(ClassEntity c, String feStatus, LocalDate today, List<Enrollment> roster) {
        Course course = c.getCourse();
        boolean online = isOnline(c);
        int studentCount = c.getEnrolled() == null ? 0 : c.getEnrolled();
        int sessionTotal = c.getTotalSessions() == null ? 0 : c.getTotalSessions();

        MyClassDto.MyClassDtoBuilder builder = MyClassDto.builder()
                .id(c.getId())
                .code(c.getLabel())
                .level(composeLevel(course))
                .courseCode(course == null ? "—" : course.getCode())
                .title(course == null ? c.getName() : course.getTitle())
                .classLabel("Lớp " + c.getLabel())
                .location(composeLocation(c))
                .isOnline(online)
                .schedule(c.getSchedule())
                .studentCount(studentCount)
                .capacity(c.getCapacity())
                .sessionTotal(sessionTotal)
                .sessionCurrent(computeSessionCurrent(c, feStatus, today, sessionTotal))
                .status(feStatus)
                .studentsPreview(buildPreview(roster));

        if (STATUS_ENDED.equals(feStatus)) {
            builder.endedAt(c.getEndDate() == null ? null : c.getEndDate().format(ENDED_AT_FMT));
        } else if (STATUS_UPCOMING.equals(feStatus) && c.getStartDate() != null) {
            long days = ChronoUnit.DAYS.between(today, c.getStartDate());
            builder.daysRemaining((int) Math.max(0, days));
        }

        return builder.build();
    }

    private List<MyClassStudentPreviewDto> buildPreview(List<Enrollment> roster) {
        List<MyClassStudentPreviewDto> preview = new ArrayList<>(Math.min(PREVIEW_LIMIT, roster.size()));
        for (int i = 0; i < roster.size() && i < PREVIEW_LIMIT; i++) {
            Enrollment e = roster.get(i);
            preview.add(MyClassStudentPreviewDto.builder()
                    .initials(deriveInitials(e.getStudentName()))
                    .toneSeed((int) (e.getId() % Integer.MAX_VALUE))
                    .build());
        }
        return preview;
    }

    /**
     * Number of scheduled sessions that have already occurred. Counts day-schedule
     * hits from start_date up to today (capped at end_date and total_sessions).
     * Upcoming classes report 0; ended classes report the full count.
     */
    private int computeSessionCurrent(ClassEntity c, String feStatus, LocalDate today, int sessionTotal) {
        if (STATUS_UPCOMING.equals(feStatus)) return 0;
        if (STATUS_ENDED.equals(feStatus)) return sessionTotal;
        if (c.getStartDate() == null) return 0;

        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (ClassDaySchedule ds : c.getDaySchedules()) {
            DayOfWeek dow = DAY_KEY_TO_DOW.get(ds.getDay());
            if (dow != null) days.add(dow);
        }
        if (days.isEmpty()) return 0;

        LocalDate end = c.getEndDate() != null && today.isAfter(c.getEndDate()) ? c.getEndDate() : today;
        int count = 0;
        for (LocalDate d = c.getStartDate(); !d.isAfter(end); d = d.plusDays(1)) {
            if (days.contains(d.getDayOfWeek())) count++;
        }
        return sessionTotal > 0 ? Math.min(count, sessionTotal) : count;
    }

    // ----- helpers -----

    private Map<Long, List<Enrollment>> loadRoster(List<ClassEntity> entities) {
        if (entities.isEmpty()) return Map.of();
        List<Long> ids = entities.stream().map(ClassEntity::getId).toList();
        Map<Long, List<Enrollment>> grouped = new HashMap<>();
        for (Enrollment e : enrollmentRepository.findRosterByClassIds(ids)) {
            if (e.getAssignedClass() == null) continue;
            grouped.computeIfAbsent(e.getAssignedClass().getId(), k -> new ArrayList<>()).add(e);
        }
        return grouped;
    }

    private String toFeStatus(String displayStatus) {
        return switch (displayStatus) {
            case ClassLifecyclePolicy.DISPLAY_ONGOING -> STATUS_RUNNING;
            case ClassLifecyclePolicy.DISPLAY_COMPLETED -> STATUS_ENDED;
            case ClassLifecyclePolicy.DISPLAY_OPEN, ClassLifecyclePolicy.DISPLAY_FULL -> STATUS_UPCOMING;
            default -> null; // draft / unpublished / cancelled are filtered out upstream
        };
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return STATUS_ALL;
        String s = status.trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case STATUS_RUNNING, STATUS_UPCOMING, STATUS_ENDED -> s;
            default -> STATUS_ALL;
        };
    }

    private boolean isOnline(ClassEntity c) {
        return c.getLocation() != null && ONLINE.equalsIgnoreCase(c.getLocation().trim());
    }

    private String composeLocation(ClassEntity c) {
        String location = c.getLocation();
        String room = c.getRoom();
        if (room != null && !room.isBlank()) {
            return (location == null ? "" : location) + " · " + room.trim();
        }
        return location;
    }

    private String composeLevel(Course course) {
        if (course == null || course.getMinAge() == null || course.getMaxAge() == null) return "";
        return course.getMinAge() + "–" + course.getMaxAge() + " tuổi";
    }

    private String deriveInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "··";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            String only = parts[0];
            return only.length() >= 2 ? only.substring(0, 2).toUpperCase() : only.toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
