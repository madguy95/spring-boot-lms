package com.springjwt.module.classroom.business.impl;

import com.springjwt.common.exception.AppException;
import com.springjwt.module.classroom.business.ClassService;
import com.springjwt.module.classroom.domain.entity.ClassDaySchedule;
import com.springjwt.module.classroom.domain.entity.ClassEntity;
import com.springjwt.module.classroom.domain.repository.ClassRepository;
import com.springjwt.module.classroom.domain.service.ClassDomainService;
import com.springjwt.module.classroom.domain.service.ClassLifecyclePolicy;
import com.springjwt.module.classroom.model.dto.*;
import com.springjwt.module.classroom.model.request.ClassListRequest;
import com.springjwt.module.classroom.model.request.CreateClassRequest;
import com.springjwt.module.classroom.model.request.DayScheduleInput;
import com.springjwt.module.classroom.model.request.LifecycleActionRequest;
import com.springjwt.module.classroom.model.request.UpdateClassRequest;
import com.springjwt.module.course.domain.entity.Course;
import com.springjwt.module.teacher.domain.entity.Teacher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private static final List<String> WEEK_ORDER =
            List.of("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");

    private final ClassRepository classRepository;
    private final ClassDomainService classDomainService;
    private final ClassLifecyclePolicy lifecyclePolicy;

    @Override
    public Page<ClassDto> listClasses(ClassListRequest request) {
        Sort sort = buildSort(request.getSortBy(), request.getSortDirection());
        PageRequest pageable = PageRequest.of(Math.max(request.getPage() - 1, 0), request.getSize(), sort);
        String search = normalize(request.getSearch());
        LocalDate today = LocalDate.now();

        Page<ClassEntity> classes = classRepository.search(request.getStatus(), search, today, pageable);
        List<ClassDto> data = classes.stream().map(c -> toDto(c, today)).toList();
        return new PageImpl<>(data, pageable, classes.getTotalElements());
    }

    @Override
    public ClassDto getClassById(Long id) {
        return toDto(classDomainService.getClassById(id), LocalDate.now());
    }

    @Override
    @Transactional
    public ClassDto createClass(CreateClassRequest request) {
        Course course = classDomainService.getPublishedCourseOrThrow(request.getCourseId());
        Teacher teacher = classDomainService.getActiveTeacherOrThrow(request.getTeacherId());

        validateDateRange(request.getStartDate(), request.getEndDate());

        List<DayScheduleInput> sortedSchedules = sortByWeekday(request.getDaySchedules());
        String label = request.getLabel().trim();

        // Always start in DRAFT — admin must explicitly publish via the lifecycle
        // endpoint, so unfinished classes can never leak to the public catalog.
        ClassEntity entity = ClassEntity.builder()
                .label(label)
                .name(composeName(course, label))
                .course(course)
                .teacher(teacher)
                .location(request.getLocation().trim())
                .schedule(composeScheduleLabel(sortedSchedules))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .capacity(request.getCapacity())
                .enrolled(0)
                .visibility(request.getVisibility())
                .lifecycleStatus(ClassLifecyclePolicy.LIFECYCLE_DRAFT)
                .totalSessions(course.getTotalSessions())
                .build();

        replaceDaySchedules(entity, sortedSchedules);

        return toDto(classRepository.save(entity), LocalDate.now());
    }

    @Override
    @Transactional
    public ClassDto updateClass(Long id, UpdateClassRequest request) {
        ClassEntity entity = classDomainService.getClassById(id);
        LocalDate today = LocalDate.now();

        // Each field is gated by the lifecycle policy — keeps the matrix in one
        // place rather than scattered if-elses for every column.
        if (request.getCourseId() != null
                && !request.getCourseId().equals(entity.getCourse().getId())) {
            lifecyclePolicy.requireEditable(ClassLifecyclePolicy.F_COURSE, entity, today);
            Course course = classDomainService.getPublishedCourseOrThrow(request.getCourseId());
            entity.setCourse(course);
            entity.setTotalSessions(course.getTotalSessions());
        }
        if (request.getTeacherId() != null
                && !request.getTeacherId().equals(entity.getTeacher().getId())) {
            lifecyclePolicy.requireEditable(ClassLifecyclePolicy.F_TEACHER, entity, today);
            Teacher teacher = classDomainService.getActiveTeacherOrThrow(request.getTeacherId());
            entity.setTeacher(teacher);
        }

        if (request.getLabel() != null) {
            lifecyclePolicy.requireEditable(ClassLifecyclePolicy.F_LABEL, entity, today);
            entity.setLabel(request.getLabel().trim());
        }

        // Recompose denormalized name whenever course or label moved.
        if (request.getCourseId() != null || request.getLabel() != null) {
            entity.setName(composeName(entity.getCourse(), entity.getLabel()));
        }

        if (request.getLocation() != null) {
            lifecyclePolicy.requireEditable(ClassLifecyclePolicy.F_LOCATION, entity, today);
            entity.setLocation(request.getLocation().trim());
        }
        if (request.getCapacity() != null) {
            lifecyclePolicy.requireEditable(ClassLifecyclePolicy.F_CAPACITY, entity, today);
            lifecyclePolicy.validateCapacityChange(request.getCapacity(),
                    entity.getEnrolled() == null ? 0 : entity.getEnrolled());
            entity.setCapacity(request.getCapacity());
        }
        if (request.getVisibility() != null) {
            lifecyclePolicy.requireEditable(ClassLifecyclePolicy.F_VISIBILITY, entity, today);
            entity.setVisibility(request.getVisibility());
        }

        if (request.getStartDate() != null) {
            lifecyclePolicy.requireEditable(ClassLifecyclePolicy.F_START_DATE, entity, today);
            lifecyclePolicy.validateStartDateChange(request.getStartDate(), today, entity);
            entity.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            lifecyclePolicy.requireEditable(ClassLifecyclePolicy.F_END_DATE, entity, today);
            lifecyclePolicy.validateEndDateChange(request.getEndDate(), entity.getEndDate(), entity, today);
            entity.setEndDate(request.getEndDate());
        }
        validateDateRange(entity.getStartDate(), entity.getEndDate());

        if (request.getDaySchedules() != null) {
            lifecyclePolicy.requireEditable(ClassLifecyclePolicy.F_DAY_SCHEDULES, entity, today);
            if (request.getDaySchedules().isEmpty()) {
                throw new AppException("class.validation.daySchedules.min", HttpStatus.BAD_REQUEST);
            }
            List<DayScheduleInput> sortedSchedules = sortByWeekday(request.getDaySchedules());
            replaceDaySchedules(entity, sortedSchedules);
            entity.setSchedule(composeScheduleLabel(sortedSchedules));
        }

        return toDto(classRepository.save(entity), today);
    }

    @Override
    @Transactional
    public ClassDto applyLifecycleAction(Long id, LifecycleActionRequest request) {
        ClassEntity entity = classDomainService.getClassById(id);
        LocalDate today = LocalDate.now();

        String nextLifecycle = lifecyclePolicy.resolveTransition(entity, request.getAction(), today);
        entity.setLifecycleStatus(nextLifecycle);

        if ("cancel".equals(request.getAction())) {
            if (request.getReason() == null || request.getReason().isBlank()) {
                throw new AppException("class.lifecycle.reason.required", HttpStatus.BAD_REQUEST);
            }
            entity.setCancellationReason(request.getReason().trim());
        }

        return toDto(classRepository.save(entity), today);
    }

    @Override
    public ClassStatusTabsDto getStatusTabs() {
        LocalDate today = LocalDate.now();
        long draft = classRepository.countByLifecycleStatus(ClassLifecyclePolicy.LIFECYCLE_DRAFT);
        long unpublished = classRepository.countByLifecycleStatus(ClassLifecyclePolicy.LIFECYCLE_UNPUBLISHED);
        long cancelled = classRepository.countByLifecycleStatus(ClassLifecyclePolicy.LIFECYCLE_CANCELLED);
        long open = classRepository.countOpen(today);
        long full = classRepository.countFull(today);
        long ongoing = classRepository.countOngoing(today);
        long completed = classRepository.countCompleted(today);
        return ClassStatusTabsDto.builder()
                .all(draft + unpublished + cancelled + open + full + ongoing + completed)
                .draft(draft)
                .open(open)
                .full(full)
                .ongoing(ongoing)
                .completed(completed)
                .unpublished(unpublished)
                .cancelled(cancelled)
                .build();
    }

    @Override
    public ClassStatsDto getStats() {
        LocalDate today = LocalDate.now();
        long draft = classRepository.countByLifecycleStatus(ClassLifecyclePolicy.LIFECYCLE_DRAFT);
        long unpublished = classRepository.countByLifecycleStatus(ClassLifecyclePolicy.LIFECYCLE_UNPUBLISHED);
        long cancelled = classRepository.countByLifecycleStatus(ClassLifecyclePolicy.LIFECYCLE_CANCELLED);
        long open = classRepository.countOpen(today);
        long full = classRepository.countFull(today);
        long ongoing = classRepository.countOngoing(today);
        long completed = classRepository.countCompleted(today);
        return ClassStatsDto.builder()
                .total(draft + unpublished + cancelled + open + full + ongoing + completed)
                .draft(draft)
                .open(open)
                .full(full)
                .ongoing(ongoing)
                .completed(completed)
                .unpublished(unpublished)
                .cancelled(cancelled)
                .build();
    }

    // ----- helpers -----

    // Update collection in place rather than clear-then-readd. Re-adding rows
    // with the same (class_id, position) would race orphanRemoval's DELETE at
    // flush time and trip uq_class_day_schedules_position. Mirrors the
    // CourseServiceImpl.replaceSessions pattern.
    private void replaceDaySchedules(ClassEntity entity, List<DayScheduleInput> inputs) {
        Set<ClassDaySchedule> current = entity.getDaySchedules();
        if (inputs == null) {
            current.clear();
            return;
        }
        List<ClassDaySchedule> sorted = new ArrayList<>(current);
        sorted.sort(Comparator.comparing(ClassDaySchedule::getPosition));

        for (int i = sorted.size() - 1; i >= inputs.size(); i--) {
            current.remove(sorted.get(i));
        }

        for (int i = 0; i < inputs.size(); i++) {
            DayScheduleInput d = inputs.get(i);
            String day = d.getDay();
            String startTime = d.getStartTime();
            String endTime = d.getEndTime();
            if (toMinutes(endTime) <= toMinutes(startTime)) {
                throw new AppException("class.validation.daySchedule.time.order", HttpStatus.BAD_REQUEST);
            }
            if (i < sorted.size()) {
                ClassDaySchedule existing = sorted.get(i);
                existing.setPosition(i);
                existing.setDay(day);
                existing.setStartTime(startTime);
                existing.setEndTime(endTime);
            } else {
                current.add(ClassDaySchedule.builder()
                        .classEntity(entity)
                        .position(i)
                        .day(day)
                        .startTime(startTime)
                        .endTime(endTime)
                        .build());
            }
        }
    }

    private String composeName(Course course, String label) {
        String title = course.getTitle();
        String prefix = (title == null || title.isBlank())
                ? "Class"
                : title.trim().split("\\s+")[0];
        if (label == null || label.isBlank()) return prefix;
        return prefix + " · " + label.trim();
    }

    private String composeScheduleLabel(List<DayScheduleInput> schedules) {
        if (schedules == null || schedules.isEmpty()) return "—";
        String firstStart = schedules.get(0).getStartTime();
        boolean allSame = schedules.stream().allMatch(s -> Objects.equals(s.getStartTime(), firstStart));
        if (allSame) {
            return schedules.stream().map(DayScheduleInput::getDay).collect(Collectors.joining(" · "))
                    + " · " + firstStart;
        }
        return schedules.stream()
                .map(s -> s.getDay() + " " + s.getStartTime())
                .collect(Collectors.joining(" · "));
    }

    private List<DayScheduleInput> sortByWeekday(List<DayScheduleInput> arr) {
        List<DayScheduleInput> copy = new ArrayList<>(arr);
        copy.sort(Comparator.comparingInt(s -> WEEK_ORDER.indexOf(s.getDay())));
        return copy;
    }

    private int toMinutes(String hhmm) {
        String[] parts = hhmm.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new AppException("class.validation.dateRange.invalid", HttpStatus.BAD_REQUEST);
        }
    }

    private ClassDto toDto(ClassEntity entity, LocalDate today) {
        Course course = entity.getCourse();
        Teacher teacher = entity.getTeacher();

        ClassCourseDto courseDto = course == null ? null : ClassCourseDto.builder()
                .id(course.getId())
                .code(course.getCode())
                .title(course.getTitle())
                .totalSessions(course.getTotalSessions())
                .build();

        ClassTeacherDto teacherDto = null;
        if (teacher != null) {
            String fullName = (teacher.getFirstName() + " " + teacher.getLastName()).trim();
            teacherDto = ClassTeacherDto.builder()
                    .id(teacher.getId())
                    .fullName(fullName)
                    .initials(deriveInitials(fullName))
                    .build();
        }

        List<ClassDayScheduleDto> dayScheduleDtos = entity.getDaySchedules().stream()
                .map(s -> ClassDayScheduleDto.builder()
                        .day(s.getDay())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .build())
                .toList();

        return ClassDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .label(entity.getLabel())
                .location(entity.getLocation())
                .schedule(entity.getSchedule())
                .enrolled(entity.getEnrolled())
                .capacity(entity.getCapacity())
                .lifecycleStatus(entity.getLifecycleStatus())
                .status(lifecyclePolicy.deriveDisplayStatus(entity, today))
                .visibility(entity.getVisibility())
                .cancellationReason(entity.getCancellationReason())
                .totalSessions(entity.getTotalSessions())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .course(courseDto)
                .teacher(teacherDto)
                .daySchedules(dayScheduleDtos)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String deriveInitials(String fullName) {
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

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(direction, "createdAt");
        }
        String field = switch (sortBy) {
            case "createdAt", "updatedAt", "name", "lifecycleStatus", "startDate" -> sortBy;
            default -> throw new AppException("class.validation.sortBy.invalid", HttpStatus.BAD_REQUEST);
        };
        return Sort.by(direction, field);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }
}
