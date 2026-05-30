package com.springjwt.module.dashboard.business.impl;

import com.springjwt.module.classroom.domain.entity.ClassDaySchedule;
import com.springjwt.module.classroom.domain.entity.ClassEntity;
import com.springjwt.module.classroom.domain.repository.ClassRepository;
import com.springjwt.module.course.domain.entity.Course;
import com.springjwt.module.course.domain.repository.CourseRepository;
import com.springjwt.module.dashboard.business.AdminDashboardService;
import com.springjwt.module.dashboard.model.dto.ClassesKpiDto;
import com.springjwt.module.dashboard.model.dto.CoursesKpiDto;
import com.springjwt.module.dashboard.model.dto.DashboardCourseFillDto;
import com.springjwt.module.dashboard.model.dto.DashboardHeaderDto;
import com.springjwt.module.dashboard.model.dto.DashboardKpisDto;
import com.springjwt.module.dashboard.model.dto.DashboardRecentEnrollmentDto;
import com.springjwt.module.dashboard.model.dto.DashboardSummaryDto;
import com.springjwt.module.dashboard.model.dto.DashboardUpcomingClassDto;
import com.springjwt.module.dashboard.model.dto.PendingKpiDto;
import com.springjwt.module.dashboard.model.dto.StudentSampleDto;
import com.springjwt.module.dashboard.model.dto.StudentsKpiDto;
import com.springjwt.module.dashboard.model.dto.UpcomingClassBadgeDto;
import com.springjwt.module.dashboard.model.request.DashboardSummaryRequest;
import com.springjwt.module.enrollment.domain.entity.Enrollment;
import com.springjwt.module.enrollment.domain.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final String STATUS_PUBLISHED = "published";
    private static final String STATUS_ACTIVE = "active";
    private static final int RECENT_ENROLLMENTS_LIMIT = 6;
    private static final int UPCOMING_CLASSES_LIMIT = 4;
    private static final int STUDENT_SAMPLE_LIMIT = 5;
    private static final int COURSE_FILL_LIMIT = 4;
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    // Mon..Sun keys match ClassDaySchedule.day storage exactly — reused from
    // ScheduleServiceImpl so we don't duplicate the constant publicly.
    private static final List<String> DAY_KEYS =
            List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

    // Same legend mapping the schedule view uses, so the dashboard's upcoming
    // tiles share colours with the calendar.
    private static final Map<String, String> TOOL_TO_CATEGORY = Map.of(
            "scratch", "scratch",
            "python", "python",
            "web", "web",
            "robotics", "robotics",
            "arduino", "robotics",
            "game", "game_ai",
            "ai", "game_ai"
    );

    private final CourseRepository courseRepository;
    private final ClassRepository classRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary(DashboardSummaryRequest request) {
        LocalDate today = LocalDate.now(DEFAULT_ZONE);
        Instant deltaSince = resolveDeltaSince(request.getPeriod(), today);

        DashboardHeaderDto header = buildHeader(today);
        DashboardKpisDto kpis = DashboardKpisDto.builder()
                .courses(buildCoursesKpi(today, deltaSince))
                .classes(buildClassesKpi(today, deltaSince))
                .students(buildStudentsKpi(deltaSince))
                .pending(buildPendingKpi())
                .build();

        return DashboardSummaryDto.builder()
                .header(header)
                .kpis(kpis)
                .recentEnrollments(buildRecentEnrollments())
                .upcomingClasses(buildUpcomingClasses(today))
                .courseFill(buildCourseFill(today))
                .build();
    }

    // ----- header -----

    private DashboardHeaderDto buildHeader(LocalDate today) {
        String dayName = today.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String monthName = today.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String todayLabel = "Today · " + dayName + ", " + monthName + " " + today.getDayOfMonth();
        return DashboardHeaderDto.builder()
                .greetingName(currentDisplayName())
                .termLabel(null) // term scaffold isn't modelled yet; FE handles null
                .todayLabel(todayLabel)
                .build();
    }

    private String currentDisplayName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        String name = auth.getName();
        if (name == null || name.isBlank() || "anonymousUser".equals(name)) return null;
        // For email principals show the local-part — friendlier on the greeting.
        int at = name.indexOf('@');
        return at > 0 ? name.substring(0, at) : name;
    }

    // ----- KPIs -----

    private CoursesKpiDto buildCoursesKpi(LocalDate today, Instant deltaSince) {
        long active = courseRepository.countByStatus(STATUS_PUBLISHED);
        long delta = courseRepository.countByStatusAndCreatedAtAfter(STATUS_PUBLISHED, deltaSince);

        // Spark bars: enrollments submitted per day for the current Mon..Sun.
        // Same anchor as the schedule view so both widgets agree on "this week".
        LocalDate monday = mondayOf(today);
        Instant weekStart = monday.atStartOfDay(DEFAULT_ZONE).toInstant();
        List<Instant> submissions = enrollmentRepository.findSubmittedTimestampsSince(weekStart);
        long[] perDay = new long[7];
        for (Instant ts : submissions) {
            LocalDate d = ts.atZone(DEFAULT_ZONE).toLocalDate();
            int idx = (int) ChronoUnit.DAYS.between(monday, d);
            if (idx >= 0 && idx < 7) perDay[idx]++;
        }
        long peak = 0;
        for (long v : perDay) peak = Math.max(peak, v);
        List<Double> trend = new ArrayList<>(7);
        for (long v : perDay) {
            trend.add(peak == 0 ? 0.0 : (double) v / peak);
        }
        return CoursesKpiDto.builder()
                .active(active)
                .delta(delta)
                .weeklyTrend(trend)
                .build();
    }

    private ClassesKpiDto buildClassesKpi(LocalDate today, Instant deltaSince) {
        long ongoing = classRepository.countOngoing(today);
        long online = classRepository.countOngoingOnline(today);
        long offline = Math.max(0, ongoing - online);
        // Delta: classes that started in the window (Instant -> LocalDate in
        // the configured zone). Approximation but matches the "new this week"
        // semantics on the chip.
        LocalDate sinceDate = deltaSince.atZone(DEFAULT_ZONE).toLocalDate();
        long delta = classRepository.countStartedBetween(sinceDate, today);
        return ClassesKpiDto.builder()
                .running(ongoing)
                .delta(delta)
                .offline(offline)
                .online(online)
                .build();
    }

    private StudentsKpiDto buildStudentsKpi(Instant deltaSince) {
        long enrolled = enrollmentRepository.countByStatus(STATUS_ACTIVE);
        long delta = enrollmentRepository.countByStatusAndApprovedAtAfter(STATUS_ACTIVE, deltaSince);
        List<Enrollment> recent =
                enrollmentRepository.findRecentActive(PageRequest.of(0, STUDENT_SAMPLE_LIMIT));
        List<StudentSampleDto> sample = new ArrayList<>(recent.size());
        for (int i = 0; i < recent.size(); i++) {
            Enrollment e = recent.get(i);
            sample.add(StudentSampleDto.builder()
                    .initials(buildInitials(e.getStudentName()))
                    .toneSeed((int) (e.getId() % Integer.MAX_VALUE))
                    .build());
        }
        return StudentsKpiDto.builder()
                .enrolled(enrolled)
                .delta(delta)
                .sample(sample)
                .build();
    }

    private PendingKpiDto buildPendingKpi() {
        long count = enrollmentRepository.countByStatus("pending");
        String avgWait = null;
        if (count > 0) {
            Double avgSeconds = enrollmentRepository.avgPendingWaitSeconds();
            if (avgSeconds != null && avgSeconds > 0) {
                avgWait = formatWaitLabel(Duration.ofSeconds(avgSeconds.longValue()));
            }
        }
        return PendingKpiDto.builder()
                .count(count)
                .avgWaitLabel(avgWait)
                .build();
    }

    // ----- lists -----

    private List<DashboardRecentEnrollmentDto> buildRecentEnrollments() {
        List<Enrollment> rows = enrollmentRepository.findRecentForDashboard(
                PageRequest.of(0, RECENT_ENROLLMENTS_LIMIT));
        List<DashboardRecentEnrollmentDto> out = new ArrayList<>(rows.size());
        for (Enrollment e : rows) {
            Course course = e.getRequestedCourse();
            out.add(DashboardRecentEnrollmentDto.builder()
                    .id(e.getId())
                    .studentName(e.getStudentName())
                    .initials(buildInitials(e.getStudentName()))
                    .studentGrade(e.getStudentGrade())
                    .studentAge(e.getStudentAge())
                    .courseTitle(course != null ? course.getTitle() : "—")
                    .channel(normalizeChannel(e.getChannel()))
                    .submittedAt(e.getSubmittedAt())
                    .status(e.getStatus())
                    .build());
        }
        return out;
    }

    private List<DashboardUpcomingClassDto> buildUpcomingClasses(LocalDate today) {
        String dayKey = DAY_KEYS.get((today.getDayOfWeek().getValue() + 6) % 7);
        List<ClassEntity> classes = classRepository.findRunningOnDay(today, dayKey);

        record Slot(ClassEntity clazz, ClassDaySchedule day, int startMin) { }
        List<Slot> slots = new ArrayList<>();
        for (ClassEntity c : classes) {
            for (ClassDaySchedule ds : c.getDaySchedules()) {
                if (!dayKey.equals(ds.getDay())) continue;
                slots.add(new Slot(c, ds, toMinutes(ds.getStartTime())));
            }
        }
        slots.sort(Comparator.comparingInt(Slot::startMin));
        if (slots.size() > UPCOMING_CLASSES_LIMIT) {
            slots = new ArrayList<>(slots.subList(0, UPCOMING_CLASSES_LIMIT));
        }

        List<DashboardUpcomingClassDto> out = new ArrayList<>(slots.size());
        for (Slot s : slots) {
            int duration = Math.max(0, toMinutes(s.day().getEndTime()) - s.startMin());
            ClassEntity c = s.clazz();
            out.add(DashboardUpcomingClassDto.builder()
                    .id(s.day().getId())
                    .classId(c.getId())
                    .startTime(s.day().getStartTime())
                    .durationMin(duration)
                    .title(c.getName())
                    .detail(buildClassDetail(c))
                    .category(toCategory(c.getCourse()))
                    .badge(buildBadge(c))
                    .build());
        }
        return out;
    }

    private List<DashboardCourseFillDto> buildCourseFill(LocalDate today) {
        List<Object[]> rows = classRepository.findCourseFillTop(
                today, PageRequest.of(0, COURSE_FILL_LIMIT));
        List<DashboardCourseFillDto> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Long id = (Long) r[0];
            String code = (String) r[1];
            String title = (String) r[2];
            long enrolled = ((Number) r[3]).longValue();
            long capacity = ((Number) r[4]).longValue();
            out.add(DashboardCourseFillDto.builder()
                    .id(id)
                    .code(code)
                    .name(title)
                    .enrolled(enrolled)
                    .capacity(capacity)
                    .build());
        }
        return out;
    }

    // ----- helpers -----

    private Instant resolveDeltaSince(String period, LocalDate today) {
        String p = period == null ? "week" : period.toLowerCase(Locale.ROOT);
        LocalDate anchor = switch (p) {
            case "month" -> today.minusMonths(1);
            case "term" -> today.minusMonths(3);
            default -> today.minusDays(7);
        };
        return anchor.atStartOfDay(DEFAULT_ZONE).toInstant();
    }

    private LocalDate mondayOf(LocalDate date) {
        int offset = date.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        return date.minusDays(offset);
    }

    private String buildInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "··";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            String only = parts[0];
            return only.length() >= 2
                    ? only.substring(0, 2).toUpperCase()
                    : only.toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private String normalizeChannel(String channel) {
        if (channel == null) return "parent_app";
        // The FE understands the four canonical channels; "website_workshop"
        // collapses to "website" because the dashboard doesn't distinguish.
        if ("website_workshop".equals(channel)) return "website";
        return channel;
    }

    private String buildClassDetail(ClassEntity c) {
        StringBuilder sb = new StringBuilder();
        if (c.getTeacher() != null) {
            String first = c.getTeacher().getFirstName() == null ? "" : c.getTeacher().getFirstName().trim();
            String last = c.getTeacher().getLastName() == null ? "" : c.getTeacher().getLastName().trim();
            String full = (first + " " + last).trim();
            if (!full.isEmpty()) sb.append(full);
        }
        String location = c.getRoom() != null && !c.getRoom().isBlank() ? c.getRoom() : c.getLocation();
        if (location != null && !location.isBlank()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(location);
        }
        Integer enrolled = c.getEnrolled();
        if (enrolled != null) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(enrolled).append(" students");
        }
        return sb.toString();
    }

    private UpcomingClassBadgeDto buildBadge(ClassEntity c) {
        boolean online = c.getLocation() != null && "online".equalsIgnoreCase(c.getLocation().trim());
        if (online) {
            return UpcomingClassBadgeDto.builder().kind("online").label("online").build();
        }
        if (c.getCapacity() != null && c.getEnrolled() != null && c.getCapacity() > 0) {
            double pct = (double) c.getEnrolled() / c.getCapacity();
            if (pct < 0.5) {
                return UpcomingClassBadgeDto.builder().kind("low_cap").label("low cap").build();
            }
        }
        return null;
    }

    private String toCategory(Course course) {
        if (course == null || course.getTool() == null) return "scratch";
        return TOOL_TO_CATEGORY.getOrDefault(course.getTool().toLowerCase(Locale.ROOT), "scratch");
    }

    private int toMinutes(String hhmm) {
        if (hhmm == null) return 0;
        String[] parts = hhmm.split(":");
        if (parts.length < 2) return 0;
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private String formatWaitLabel(Duration d) {
        long totalMinutes = d.toMinutes();
        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;
        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }
}
