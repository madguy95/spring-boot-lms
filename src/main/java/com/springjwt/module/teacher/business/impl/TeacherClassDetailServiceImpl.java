package com.springjwt.module.teacher.business.impl;

import com.springjwt.common.exception.AppException;
import com.springjwt.module.classroom.domain.entity.ClassAttendance;
import com.springjwt.module.classroom.domain.entity.ClassEntity;
import com.springjwt.module.classroom.domain.entity.ClassSession;
import com.springjwt.module.classroom.domain.entity.ClassSessionNote;
import com.springjwt.module.classroom.domain.repository.ClassAttendanceRepository;
import com.springjwt.module.classroom.domain.repository.ClassRepository;
import com.springjwt.module.classroom.domain.repository.ClassSessionNoteRepository;
import com.springjwt.module.classroom.domain.repository.ClassSessionRepository;
import com.springjwt.module.course.domain.entity.Course;
import com.springjwt.module.enrollment.domain.entity.Enrollment;
import com.springjwt.module.enrollment.domain.repository.EnrollmentRepository;
import com.springjwt.module.teacher.business.TeacherClassDetailService;
import com.springjwt.module.teacher.domain.entity.Teacher;
import com.springjwt.module.teacher.domain.repository.TeacherRepository;
import com.springjwt.module.teacher.model.dto.*;
import com.springjwt.module.teacher.model.request.SaveSessionAttendanceRequest;
import com.springjwt.module.teacher.model.request.SaveSessionNotesRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TeacherClassDetailServiceImpl implements TeacherClassDetailService {

    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DM = DateTimeFormatter.ofPattern("dd/MM");
    private static final String[] COLORS = {"emerald", "sky", "amber", "violet", "rose", "slate"};
    private static final Set<String> ATTENDED = Set.of("present", "makeup");
    private static final Set<String> MARKED = Set.of("present", "excused", "absent", "makeup");

    private final TeacherRepository teacherRepository;
    private final ClassRepository classRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassSessionRepository sessionRepository;
    private final ClassAttendanceRepository attendanceRepository;
    private final ClassSessionNoteRepository noteRepository;

    // ----- reads ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public TeacherClassDetailDto getDetail(Long userId, Long classId) {
        ClassEntity klass = requireOwnedClass(userId, classId);
        List<Enrollment> roster = enrollmentRepository.findRosterByClassId(classId);
        List<ClassSession> sessions = sessionRepository.findByClassEntityIdOrderByIdxAsc(classId);

        int taughtCount = (int) sessions.stream().filter(s -> !"upcoming".equals(s.getStatus())).count();
        List<ClassAttendance> attendance = sessions.isEmpty()
                ? List.of()
                : attendanceRepository.findBySessionIdIn(sessions.stream().map(ClassSession::getId).toList());

        long marked = attendance.stream().filter(a -> MARKED.contains(a.getStatus())).count();
        long attended = attendance.stream().filter(a -> ATTENDED.contains(a.getStatus())).count();
        int rate = marked > 0 ? (int) Math.round(100.0 * attended / marked) : 0;

        ClassSession current = pickCurrentSession(sessions);
        ClassSession latestTaught = latestTaught(sessions);
        int presentLastWeek = 0;
        int absentLastWeek = 0;
        if (latestTaught != null) {
            for (ClassAttendance a : attendance) {
                if (!a.getSession().getId().equals(latestTaught.getId())) continue;
                if (ATTENDED.contains(a.getStatus())) presentLastWeek++;
                else if ("absent".equals(a.getStatus())) absentLastWeek++;
            }
        }

        int needsReview = 0;
        String needsReviewSession = "";
        if (current != null) {
            List<ClassSessionNote> notes = noteRepository.findBySessionId(current.getId());
            long savedPresent = notes.stream()
                    .filter(n -> "present".equals(n.getAttendance()) && Boolean.TRUE.equals(n.getSaved()))
                    .count();
            long presentInSession = attendance.stream()
                    .filter(a -> a.getSession().getId().equals(current.getId()) && "present".equals(a.getStatus()))
                    .count();
            needsReview = (int) Math.max(0, presentInSession - savedPresent);
            needsReviewSession = "buổi " + current.getIdx() + " · " + nullToEmpty(current.getDateLabel());
        }

        Course course = klass.getCourse();
        LocalDate today = LocalDate.now();
        int studentCount = roster.isEmpty() && klass.getEnrolled() != null ? klass.getEnrolled() : roster.size();

        return TeacherClassDetailDto.builder()
                .id(String.valueOf(klass.getId()))
                .classLabel("Lớp " + klass.getLabel())
                .courseCode(course == null ? "—" : course.getCode())
                .courseName(course == null ? klass.getName() : course.getTitle())
                .status(deriveStatus(klass, today))
                .title((course == null ? klass.getName() : course.getTitle()) + " · Lớp " + klass.getLabel())
                .location(composeLocation(klass))
                .schedule(klass.getSchedule())
                .termRange(formatTermRange(klass))
                .sessionCurrent(taughtCount)
                .sessionTotal(klass.getTotalSessions() == null ? sessions.size() : klass.getTotalSessions())
                .studentCount(studentCount)
                .presentLastWeek(presentLastWeek)
                .absentLastWeek(absentLastWeek)
                .attendanceRate(rate)
                .attendedCount((int) attended)
                .attendedTotal((int) marked)
                .needsReviewCount(needsReview)
                .needsReviewSession(needsReviewSession)
                .color(pickColor(klass.getId()))
                .currentSessionId(current == null ? "" : current.getCode())
                .coverUrl(course == null ? null : course.getCoverUrl())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherClassStudentsDto getStudents(Long userId, Long classId, String search) {
        requireOwnedClass(userId, classId);
        List<Enrollment> roster = enrollmentRepository.findRosterByClassId(classId);
        List<ClassSession> sessions = sessionRepository.findByClassEntityIdOrderByIdxAsc(classId);
        int totalSessions = (int) sessions.stream().filter(s -> !"upcoming".equals(s.getStatus())).count();
        List<ClassAttendance> attendance = sessions.isEmpty()
                ? List.of()
                : attendanceRepository.findBySessionIdIn(sessions.stream().map(ClassSession::getId).toList());
        ClassSession latest = latestTaught(sessions);

        List<TeacherRosterStudentDto> all = new ArrayList<>(roster.size());
        for (Enrollment e : roster) {
            int attended = (int) attendance.stream()
                    .filter(a -> a.getEnrollment().getId().equals(e.getId()) && ATTENDED.contains(a.getStatus()))
                    .count();
            String lastStatus = "unmarked";
            String lastLabel = "";
            if (latest != null) {
                lastLabel = latest.getCode() + " " + nullToEmpty(latest.getDateLabel());
                lastStatus = attendance.stream()
                        .filter(a -> a.getSession().getId().equals(latest.getId())
                                && a.getEnrollment().getId().equals(e.getId()))
                        .map(ClassAttendance::getStatus)
                        .findFirst().orElse("unmarked");
            }
            all.add(TeacherRosterStudentDto.builder()
                    .id(String.valueOf(e.getId()))
                    .name(e.getStudentName())
                    .initials(deriveInitials(e.getStudentName()))
                    .toneSeed((int) (e.getId() % Integer.MAX_VALUE))
                    .age(e.getStudentAge())
                    .grade(e.getStudentGrade())
                    .parentName(e.getParentName())
                    .parentPhone(e.getParentPhone())
                    .attendedSessions(attended)
                    .totalSessions(totalSessions)
                    .lastStatus(lastStatus)
                    .lastSessionLabel(lastLabel)
                    .build());
        }

        List<TeacherRosterStudentDto> filtered = all;
        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase(Locale.ROOT);
            filtered = all.stream().filter(s ->
                    contains(s.getName(), q) || contains(s.getId(), q)
                            || contains(s.getParentName(), q) || contains(s.getParentPhone(), q)).toList();
        }

        return TeacherClassStudentsDto.builder()
                .students(filtered)
                .total(filtered.size())
                .totalAll(all.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherClassSessionsDto getSessions(Long userId, Long classId) {
        requireOwnedClass(userId, classId);
        List<ClassSession> sessions = sessionRepository.findByClassEntityIdOrderByIdxAsc(classId);
        Map<Long, Long> reviewBySession = needsReviewBySession(sessions);

        List<TeacherClassSessionDto> dtos = new ArrayList<>(sessions.size());
        for (ClassSession s : sessions) {
            dtos.add(TeacherClassSessionDto.builder()
                    .id(s.getCode())
                    .index(s.getIdx())
                    .dateLabel(s.getDateLabel())
                    .title(s.getTitle())
                    .status(s.getStatus())
                    .needsReviewCount("in_progress".equals(s.getStatus())
                            ? reviewBySession.getOrDefault(s.getId(), 0L).intValue() : null)
                    .build());
        }
        ClassSession current = pickCurrentSession(sessions);
        return TeacherClassSessionsDto.builder()
                .sessions(dtos)
                .currentSessionId(current == null ? "" : current.getCode())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherSessionAttendanceDto getAttendance(Long userId, Long classId, String sessionCode) {
        requireOwnedClass(userId, classId);
        ClassSession session = requireSession(classId, sessionCode);
        List<Enrollment> roster = enrollmentRepository.findRosterByClassId(classId);
        Map<Long, String> statusByEnrollment = new HashMap<>();
        Map<Long, String> noteByEnrollment = new HashMap<>();
        for (ClassAttendance a : attendanceRepository.findBySessionId(session.getId())) {
            statusByEnrollment.put(a.getEnrollment().getId(), a.getStatus());
            if (a.getNote() != null && !a.getNote().isBlank()) {
                noteByEnrollment.put(a.getEnrollment().getId(), a.getNote());
            }
        }
        Map<String, String> marks = new LinkedHashMap<>();
        Map<String, String> notes = new LinkedHashMap<>();
        for (Enrollment e : roster) {
            String key = String.valueOf(e.getId());
            marks.put(key, statusByEnrollment.getOrDefault(e.getId(), "unmarked"));
            String note = noteByEnrollment.get(e.getId());
            if (note != null) notes.put(key, note);
        }
        return TeacherSessionAttendanceDto.builder()
                .sessionId(session.getCode())
                .meta(toMeta(session))
                .marks(marks)
                .notes(notes)
                .build();
    }

    @Override
    @Transactional
    public TeacherSessionAttendanceDto saveAttendance(Long userId, Long classId, String sessionCode,
                                                      SaveSessionAttendanceRequest request) {
        requireOwnedClass(userId, classId);
        ClassSession session = requireSession(classId, sessionCode);
        Map<Long, Enrollment> roster = rosterById(classId);
        Map<Long, ClassAttendance> existing = new HashMap<>();
        for (ClassAttendance a : attendanceRepository.findBySessionId(session.getId())) {
            existing.put(a.getEnrollment().getId(), a);
        }
        Map<String, String> marks = request == null || request.getMarks() == null ? Map.of() : request.getMarks();
        Map<String, String> notes = request == null || request.getNotes() == null ? Map.of() : request.getNotes();
        List<ClassAttendance> toSave = new ArrayList<>();
        for (Map.Entry<String, String> entry : marks.entrySet()) {
            Long enrollmentId = parseLong(entry.getKey());
            Enrollment student = enrollmentId == null ? null : roster.get(enrollmentId);
            if (student == null) continue; // ignore ids not in this class roster
            String status = normalizeAttendance(entry.getValue());
            String note = blankToNull(notes.get(entry.getKey()));
            ClassAttendance row = existing.get(enrollmentId);
            if (row == null) {
                row = ClassAttendance.builder().session(session).enrollment(student)
                        .status(status).note(note).build();
            } else {
                row.setStatus(status);
                row.setNote(note);
            }
            toSave.add(row);
        }
        attendanceRepository.saveAll(toSave);
        return getAttendance(userId, classId, sessionCode);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherSessionNotesDto getNotes(Long userId, Long classId, String sessionCode) {
        requireOwnedClass(userId, classId);
        ClassSession session = requireSession(classId, sessionCode);
        List<Enrollment> roster = enrollmentRepository.findRosterByClassId(classId);

        Map<Long, String> attendanceByEnrollment = new HashMap<>();
        for (ClassAttendance a : attendanceRepository.findBySessionId(session.getId())) {
            attendanceByEnrollment.put(a.getEnrollment().getId(), a.getStatus());
        }
        Map<Long, ClassSessionNote> noteByEnrollment = new HashMap<>();
        for (ClassSessionNote n : noteRepository.findBySessionId(session.getId())) {
            noteByEnrollment.put(n.getEnrollment().getId(), n);
        }

        List<TeacherStudentNoteDto> notes = new ArrayList<>(roster.size());
        for (Enrollment e : roster) {
            ClassSessionNote n = noteByEnrollment.get(e.getId());
            String attendance = attendanceByEnrollment.getOrDefault(e.getId(),
                    n != null ? n.getAttendance() : "present");
            notes.add(TeacherStudentNoteDto.builder()
                    .studentId(String.valueOf(e.getId()))
                    .attendance(attendance)
                    .note(n == null ? null : n.getNote())
                    .rating(n == null ? null : n.getRating())
                    .tags(n == null ? null : splitTags(n.getTags()))
                    .saved(n != null && Boolean.TRUE.equals(n.getSaved()))
                    .build());
        }

        return TeacherSessionNotesDto.builder()
                .sessionId(session.getCode())
                .meta(toMeta(session))
                .summary(TeacherSessionSummaryDto.builder()
                        .comment(nullToEmpty(session.getSummaryComment()))
                        .rating(session.getSummaryRating() == null ? "good" : session.getSummaryRating())
                        .build())
                .notes(notes)
                .build();
    }

    @Override
    @Transactional
    public TeacherSessionNotesDto saveNotes(Long userId, Long classId, String sessionCode,
                                            SaveSessionNotesRequest request) {
        requireOwnedClass(userId, classId);
        ClassSession session = requireSession(classId, sessionCode);

        if (request != null && request.getSummary() != null) {
            session.setSummaryComment(request.getSummary().getComment());
            session.setSummaryRating(normalizeRating(request.getSummary().getRating()));
            sessionRepository.save(session);
        }

        Map<Long, Enrollment> roster = rosterById(classId);
        Map<Long, ClassSessionNote> existing = new HashMap<>();
        for (ClassSessionNote n : noteRepository.findBySessionId(session.getId())) {
            existing.put(n.getEnrollment().getId(), n);
        }
        List<TeacherStudentNoteDto> incoming = request == null || request.getNotes() == null
                ? List.of() : request.getNotes();
        List<ClassSessionNote> toSave = new ArrayList<>();
        for (TeacherStudentNoteDto dto : incoming) {
            Long enrollmentId = parseLong(dto.getStudentId());
            Enrollment student = enrollmentId == null ? null : roster.get(enrollmentId);
            if (student == null) continue;
            ClassSessionNote row = existing.get(enrollmentId);
            if (row == null) {
                row = ClassSessionNote.builder().session(session).enrollment(student).build();
            }
            row.setAttendance(normalizeAttendance(dto.getAttendance()));
            row.setNote(dto.getNote());
            row.setRating(normalizeRating(dto.getRating()));
            row.setTags(joinTags(dto.getTags()));
            row.setSaved(dto.getSaved() == null ? Boolean.FALSE : dto.getSaved());
            toSave.add(row);
        }
        noteRepository.saveAll(toSave);
        return getNotes(userId, classId, sessionCode);
    }

    // ----- helpers -------------------------------------------------------------

    private ClassEntity requireOwnedClass(Long userId, Long classId) {
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException("teacher.not.found", HttpStatus.NOT_FOUND));
        ClassEntity klass = classRepository.findById(classId)
                .orElseThrow(() -> new AppException("class.not.found", HttpStatus.NOT_FOUND));
        if (klass.getTeacher() == null || !klass.getTeacher().getId().equals(teacher.getId())) {
            throw new AppException("class.forbidden", HttpStatus.FORBIDDEN);
        }
        return klass;
    }

    private ClassSession requireSession(Long classId, String sessionCode) {
        return sessionRepository.findByClassEntityIdAndCode(classId, sessionCode)
                .orElseThrow(() -> new AppException("class.session.not.found", HttpStatus.NOT_FOUND));
    }

    private Map<Long, Enrollment> rosterById(Long classId) {
        Map<Long, Enrollment> map = new HashMap<>();
        for (Enrollment e : enrollmentRepository.findRosterByClassId(classId)) {
            map.put(e.getId(), e);
        }
        return map;
    }

    private Map<Long, Long> needsReviewBySession(List<ClassSession> sessions) {
        Map<Long, Long> result = new HashMap<>();
        for (ClassSession s : sessions) {
            if (!"in_progress".equals(s.getStatus())) continue;
            long present = attendanceRepository.findBySessionId(s.getId()).stream()
                    .filter(a -> "present".equals(a.getStatus())).count();
            long savedPresent = noteRepository.findBySessionId(s.getId()).stream()
                    .filter(n -> "present".equals(n.getAttendance()) && Boolean.TRUE.equals(n.getSaved()))
                    .count();
            result.put(s.getId(), Math.max(0, present - savedPresent));
        }
        return result;
    }

    private ClassSession pickCurrentSession(List<ClassSession> sessions) {
        ClassSession inProgress = sessions.stream()
                .filter(s -> "in_progress".equals(s.getStatus())).findFirst().orElse(null);
        if (inProgress != null) return inProgress;
        ClassSession taught = latestTaught(sessions);
        if (taught != null) return taught;
        return sessions.isEmpty() ? null : sessions.get(sessions.size() - 1);
    }

    private ClassSession latestTaught(List<ClassSession> sessions) {
        ClassSession latest = null;
        for (ClassSession s : sessions) {
            if (!"upcoming".equals(s.getStatus())) latest = s;
        }
        return latest;
    }

    private TeacherSessionMetaDto toMeta(ClassSession s) {
        return TeacherSessionMetaDto.builder()
                .id(s.getCode())
                .dayLabel(s.getDayLabel())
                .title(s.getTitle())
                .description(s.getDescription())
                .timeLabel(s.getTimeLabel())
                .build();
    }

    private String deriveStatus(ClassEntity c, LocalDate today) {
        if (c.getStartDate() != null && today.isBefore(c.getStartDate())) return "upcoming";
        if (c.getEndDate() != null && today.isAfter(c.getEndDate())) return "ended";
        return "running";
    }

    private String composeLocation(ClassEntity c) {
        String location = c.getLocation();
        String room = c.getRoom();
        if (room != null && !room.isBlank()) {
            return (location == null ? "" : location) + " · " + room.trim();
        }
        return location;
    }

    private String formatTermRange(ClassEntity c) {
        if (c.getStartDate() == null || c.getEndDate() == null) return "";
        return c.getStartDate().format(DM) + " — " + c.getEndDate().format(DMY);
    }

    private String pickColor(Long id) {
        int h = (int) (Math.abs(id == null ? 0 : id) % COLORS.length);
        return COLORS[h];
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

    private boolean contains(String value, String lowerQuery) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowerQuery);
    }

    private List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) return List.of();
        return List.of(tags.split("\\s*,\\s*"));
    }

    private String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return null;
        return String.join(",", tags);
    }

    private String normalizeAttendance(String status) {
        if (status == null) return "unmarked";
        String s = status.trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "present", "excused", "absent", "makeup", "unmarked" -> s;
            default -> "unmarked";
        };
    }

    private String normalizeRating(String rating) {
        if (rating == null || rating.isBlank()) return null;
        String r = rating.trim().toLowerCase(Locale.ROOT);
        return switch (r) {
            case "weak", "average", "good", "great", "excellent" -> r;
            default -> null;
        };
    }

    private String nullToEmpty(String v) {
        return v == null ? "" : v;
    }

    private String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }

    private Long parseLong(String v) {
        try {
            return v == null ? null : Long.parseLong(v.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
