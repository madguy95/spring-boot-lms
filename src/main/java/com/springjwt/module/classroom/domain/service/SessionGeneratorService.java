package com.springjwt.module.classroom.domain.service;

import com.springjwt.module.classroom.domain.entity.ClassDaySchedule;
import com.springjwt.module.classroom.domain.entity.ClassEntity;
import com.springjwt.module.classroom.domain.entity.ClassSession;
import com.springjwt.module.classroom.domain.repository.ClassSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SessionGeneratorService {

    private static final DateTimeFormatter DM  = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Map<String, DayOfWeek> DAY_MAP = Map.of(
            "Sun", DayOfWeek.SUNDAY,
            "Mon", DayOfWeek.MONDAY,
            "Tue", DayOfWeek.TUESDAY,
            "Wed", DayOfWeek.WEDNESDAY,
            "Thu", DayOfWeek.THURSDAY,
            "Fri", DayOfWeek.FRIDAY,
            "Sat", DayOfWeek.SATURDAY
    );

    private static final Map<DayOfWeek, String> VN_DAY = Map.of(
            DayOfWeek.SUNDAY,    "CN",
            DayOfWeek.MONDAY,    "T2",
            DayOfWeek.TUESDAY,   "T3",
            DayOfWeek.WEDNESDAY, "T4",
            DayOfWeek.THURSDAY,  "T5",
            DayOfWeek.FRIDAY,    "T6",
            DayOfWeek.SATURDAY,  "T7"
    );

    private final ClassSessionRepository sessionRepository;

    /**
     * Generates ClassSession rows for a class based on its ClassDaySchedule entries.
     * Walks [startDate, endDate], creates one session per matching weekday slot,
     * stopping at totalSessions if set.
     *
     * No-op if the class has no day schedules.
     */
    public void generateSessions(ClassEntity klass) {
        List<ClassDaySchedule> schedules = klass.getDaySchedules().stream()
                .sorted(Comparator.comparing(ClassDaySchedule::getPosition))
                .toList();

        if (schedules.isEmpty()) return;

        LocalDate start        = klass.getStartDate();
        LocalDate end          = klass.getEndDate();
        int       totalSessions = klass.getTotalSessions() == null ? 0 : klass.getTotalSessions();
        String    room          = klass.getRoom();
        LocalDate today         = LocalDate.now();

        // Group schedule slots by weekday (preserves position order within each day).
        LinkedHashMap<DayOfWeek, List<ClassDaySchedule>> byDay = new LinkedHashMap<>();
        for (ClassDaySchedule s : schedules) {
            DayOfWeek dow = DAY_MAP.get(s.getDay());
            if (dow != null) byDay.computeIfAbsent(dow, k -> new ArrayList<>()).add(s);
        }

        List<ClassSession> sessions = new ArrayList<>();
        int     idx    = 1;
        LocalDate cursor = start;

        while (!cursor.isAfter(end)) {
            if (totalSessions > 0 && idx > totalSessions) break;
            List<ClassDaySchedule> slots = byDay.get(cursor.getDayOfWeek());
            if (slots != null) {
                for (ClassDaySchedule slot : slots) {
                    if (totalSessions > 0 && idx > totalSessions) break;
                    String timeRange  = slot.getStartTime() + "–" + slot.getEndTime();
                    String roomSuffix = (room != null && !room.isBlank()) ? " · " + room.trim() : "";
                    String dayVn      = VN_DAY.get(cursor.getDayOfWeek());
                    sessions.add(ClassSession.builder()
                            .classEntity(klass)
                            .idx(idx)
                            .code("B" + idx)
                            .sessionDate(cursor)
                            .dateLabel(cursor.format(DM))
                            .dayLabel(dayVn + " · " + cursor.format(DMY) + " · " + timeRange)
                            .timeLabel(timeRange + roomSuffix)
                            .title("Buổi " + idx)
                            .status(deriveStatus(cursor, today))
                            .build());
                    idx++;
                }
            }
            cursor = cursor.plusDays(1);
        }

        sessionRepository.saveAll(sessions);
    }

    private String deriveStatus(LocalDate date, LocalDate today) {
        if (date.isBefore(today)) return "reviewed";
        if (date.isEqual(today))  return "in_progress";
        return "upcoming";
    }
}
