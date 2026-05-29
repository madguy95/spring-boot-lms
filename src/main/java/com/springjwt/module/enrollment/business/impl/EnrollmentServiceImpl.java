package com.springjwt.module.enrollment.business.impl;

import com.springjwt.common.exception.AppException;
import com.springjwt.module.classroom.domain.entity.ClassEntity;
import com.springjwt.module.classroom.domain.repository.ClassRepository;
import com.springjwt.module.course.domain.entity.Course;
import com.springjwt.module.enrollment.business.EnrollmentService;
import com.springjwt.module.enrollment.domain.entity.Enrollment;
import com.springjwt.module.enrollment.domain.repository.EnrollmentRepository;
import com.springjwt.module.enrollment.domain.service.EnrollmentDomainService;
import com.springjwt.module.enrollment.model.dto.BulkActionResultDto;
import com.springjwt.module.enrollment.model.dto.EnrollmentClassDto;
import com.springjwt.module.enrollment.model.dto.EnrollmentCourseDto;
import com.springjwt.module.enrollment.model.dto.EnrollmentDto;
import com.springjwt.module.enrollment.model.dto.EnrollmentStatusTabsDto;
import com.springjwt.module.enrollment.model.request.ApproveEnrollmentRequest;
import com.springjwt.module.enrollment.model.request.BulkActionRequest;
import com.springjwt.module.enrollment.model.request.CreateEnrollmentRequest;
import com.springjwt.module.enrollment.model.request.EnrollmentListRequest;
import com.springjwt.module.enrollment.model.request.RejectEnrollmentRequest;
import com.springjwt.module.enrollment.model.request.UpdatePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_WAITLIST = "waitlist";
    private static final String STATUS_REJECTED = "rejected";

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentDomainService enrollmentDomainService;
    private final ClassRepository classRepository;

    @Override
    public Page<EnrollmentDto> listEnrollments(EnrollmentListRequest request) {
        Sort sort = buildSort(request.getSortBy(), request.getSortDirection());
        PageRequest pageable = PageRequest.of(Math.max(request.getPage() - 1, 0), request.getSize(), sort);
        String search = normalize(request.getSearch());

        Page<Enrollment> page = enrollmentRepository.search(request.getStatus(), search, pageable);
        List<EnrollmentDto> data = page.stream().map(this::toDto).toList();
        return new PageImpl<>(data, pageable, page.getTotalElements());
    }

    @Override
    public EnrollmentDto getEnrollmentById(Long id) {
        return toDto(enrollmentDomainService.getEnrollmentById(id));
    }

    @Override
    @Transactional
    public EnrollmentDto createEnrollment(CreateEnrollmentRequest request) {
        Course course = enrollmentDomainService.getCourseOrThrow(request.getRequestedCourseId());

        Enrollment enrollment = Enrollment.builder()
                .studentName(request.getStudentName().trim())
                .studentAge(request.getStudentAge())
                .studentGrade(request.getStudentGrade())
                .parentName(request.getParentName().trim())
                .parentPhone(normalize(request.getParentPhone()))
                .parentEmail(normalize(request.getParentEmail()))
                .requestedCourse(course)
                .note(normalize(request.getNote()))
                .status(STATUS_PENDING)
                .channel(request.getChannel() == null ? "parent_app" : request.getChannel())
                .paymentAmount(request.getPaymentAmount())
                .paymentStatus(normalize(request.getPaymentStatus()))
                .submittedAt(Instant.now())
                .build();

        return toDto(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public EnrollmentDto approveEnrollment(Long id, ApproveEnrollmentRequest request) {
        Enrollment enrollment = enrollmentDomainService.getEnrollmentById(id);
        if (STATUS_ACTIVE.equals(enrollment.getStatus())) {
            throw new AppException("enrollment.already.active", HttpStatus.CONFLICT);
        }

        ClassEntity cls = enrollmentDomainService.getAssignableClassOrThrow(request.getClassId());

        // Bump the class's enrolled counter — keeps tabs/full-state in sync without
        // a second pass over the data.
        cls.setEnrolled((cls.getEnrolled() == null ? 0 : cls.getEnrolled()) + 1);
        classRepository.save(cls);

        enrollment.setAssignedClass(cls);
        enrollment.setStatus(STATUS_ACTIVE);
        enrollment.setApprovedAt(Instant.now());
        enrollment.setRejectionReason(null);
        return toDto(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public EnrollmentDto waitlistEnrollment(Long id) {
        Enrollment enrollment = enrollmentDomainService.getEnrollmentById(id);
        if (STATUS_WAITLIST.equals(enrollment.getStatus())) {
            return toDto(enrollment);
        }
        // If we are moving away from active, free up the seat we previously took.
        if (STATUS_ACTIVE.equals(enrollment.getStatus()) && enrollment.getAssignedClass() != null) {
            releaseSeat(enrollment.getAssignedClass());
            enrollment.setAssignedClass(null);
        }
        enrollment.setStatus(STATUS_WAITLIST);
        enrollment.setWaitlistedAt(Instant.now());
        enrollment.setRejectionReason(null);
        return toDto(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public EnrollmentDto rejectEnrollment(Long id, RejectEnrollmentRequest request) {
        Enrollment enrollment = enrollmentDomainService.getEnrollmentById(id);
        if (STATUS_ACTIVE.equals(enrollment.getStatus()) && enrollment.getAssignedClass() != null) {
            releaseSeat(enrollment.getAssignedClass());
            enrollment.setAssignedClass(null);
        }
        enrollment.setStatus(STATUS_REJECTED);
        enrollment.setRejectedAt(Instant.now());
        enrollment.setRejectionReason(request.getReason().trim());
        return toDto(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public EnrollmentDto updatePayment(Long id, UpdatePaymentRequest request) {
        Enrollment enrollment = enrollmentDomainService.getEnrollmentById(id);
        enrollment.setPaymentStatus(request.getStatus());
        // Only overwrite the amount when the admin actually provided one — null
        // means "status flip only, keep the existing recorded amount".
        if (request.getAmount() != null) {
            enrollment.setPaymentAmount(request.getAmount());
        }
        return toDto(enrollmentRepository.save(enrollment));
    }

    @Override
    public EnrollmentStatusTabsDto getStatusTabs() {
        long pending = enrollmentRepository.countByStatus(STATUS_PENDING);
        long active = enrollmentRepository.countByStatus(STATUS_ACTIVE);
        long waitlist = enrollmentRepository.countByStatus(STATUS_WAITLIST);
        long rejected = enrollmentRepository.countByStatus(STATUS_REJECTED);
        return EnrollmentStatusTabsDto.builder()
                .all(pending + active + waitlist + rejected)
                .pending(pending)
                .active(active)
                .waitlist(waitlist)
                .rejected(rejected)
                .build();
    }

    @Override
    @Transactional
    public BulkActionResultDto bulkAction(BulkActionRequest request) {
        if (request.getAction() == null) {
            throw new AppException("enrollment.validation.bulk.action.invalid", HttpStatus.BAD_REQUEST);
        }
        List<Long> updated = new ArrayList<>();
        List<Long> skipped = new ArrayList<>();

        boolean isApprove = "approve".equals(request.getAction());
        ClassEntity sharedClass = null;
        if (isApprove) {
            if (request.getClassId() == null) {
                throw new AppException("enrollment.validation.classId.required", HttpStatus.BAD_REQUEST);
            }
            sharedClass = enrollmentDomainService.getAssignableClassOrThrow(request.getClassId());
        }

        for (Long id : request.getIds()) {
            Enrollment enrollment = enrollmentRepository.findById(id).orElse(null);
            if (enrollment == null) {
                skipped.add(id);
                continue;
            }
            try {
                if (isApprove) {
                    int capacity = sharedClass.getCapacity() == null ? 0 : sharedClass.getCapacity();
                    int enrolled = sharedClass.getEnrolled() == null ? 0 : sharedClass.getEnrolled();
                    if (enrolled >= capacity) {
                        // No room left — stop the loop early so the caller knows the
                        // remaining ids weren't attempted.
                        skipped.add(id);
                        continue;
                    }
                    if (STATUS_ACTIVE.equals(enrollment.getStatus())) {
                        skipped.add(id);
                        continue;
                    }
                    sharedClass.setEnrolled(enrolled + 1);
                    enrollment.setAssignedClass(sharedClass);
                    enrollment.setStatus(STATUS_ACTIVE);
                    enrollment.setApprovedAt(Instant.now());
                    enrollment.setRejectionReason(null);
                } else {
                    if (STATUS_WAITLIST.equals(enrollment.getStatus())) {
                        skipped.add(id);
                        continue;
                    }
                    if (STATUS_ACTIVE.equals(enrollment.getStatus()) && enrollment.getAssignedClass() != null) {
                        releaseSeat(enrollment.getAssignedClass());
                        enrollment.setAssignedClass(null);
                    }
                    enrollment.setStatus(STATUS_WAITLIST);
                    enrollment.setWaitlistedAt(Instant.now());
                    enrollment.setRejectionReason(null);
                }
                enrollmentRepository.save(enrollment);
                updated.add(id);
            } catch (Exception ex) {
                skipped.add(id);
            }
        }

        if (isApprove && sharedClass != null) {
            classRepository.save(sharedClass);
        }

        return BulkActionResultDto.builder()
                .updated(updated.size())
                .skipped(skipped.size())
                .updatedIds(updated)
                .skippedIds(skipped)
                .build();
    }

    // ----- helpers -----

    private void releaseSeat(ClassEntity cls) {
        int enrolled = cls.getEnrolled() == null ? 0 : cls.getEnrolled();
        cls.setEnrolled(Math.max(0, enrolled - 1));
        classRepository.save(cls);
    }

    private EnrollmentDto toDto(Enrollment e) {
        Course course = e.getRequestedCourse();
        EnrollmentCourseDto courseDto = course == null ? null : EnrollmentCourseDto.builder()
                .id(course.getId())
                .code(course.getCode())
                .title(course.getTitle())
                .totalSessions(course.getTotalSessions())
                .minAge(course.getMinAge())
                .maxAge(course.getMaxAge())
                .build();

        ClassEntity cls = e.getAssignedClass();
        EnrollmentClassDto classDto = cls == null ? null : EnrollmentClassDto.builder()
                .id(cls.getId())
                .name(cls.getName())
                .label(cls.getLabel())
                .schedule(cls.getSchedule())
                .enrolled(cls.getEnrolled())
                .capacity(cls.getCapacity())
                .build();

        return EnrollmentDto.builder()
                .id(e.getId())
                .studentName(e.getStudentName())
                .initials(buildInitials(e.getStudentName()))
                .studentAge(e.getStudentAge())
                .studentGrade(e.getStudentGrade())
                .parentName(e.getParentName())
                .parentPhone(e.getParentPhone())
                .parentEmail(e.getParentEmail())
                .note(e.getNote())
                .status(e.getStatus())
                .channel(e.getChannel())
                .rejectionReason(e.getRejectionReason())
                .paymentAmount(e.getPaymentAmount())
                .paymentStatus(e.getPaymentStatus())
                .submittedAt(e.getSubmittedAt())
                .approvedAt(e.getApprovedAt())
                .waitlistedAt(e.getWaitlistedAt())
                .rejectedAt(e.getRejectedAt())
                .requestedCourse(courseDto)
                .assignedClass(classDto)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
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

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(direction, "submittedAt");
        }
        String field = switch (sortBy) {
            case "submittedAt", "createdAt", "updatedAt", "status", "studentName" -> sortBy;
            default -> throw new AppException("enrollment.validation.sortBy.invalid", HttpStatus.BAD_REQUEST);
        };
        return Sort.by(direction, field);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }
}
