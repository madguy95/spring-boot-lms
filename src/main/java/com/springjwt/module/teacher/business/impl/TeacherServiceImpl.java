package com.springjwt.module.teacher.business.impl;

import com.springjwt.common.enums.ERole;
import com.springjwt.common.exception.AppException;
import com.springjwt.module.teacher.business.TeacherService;
import com.springjwt.module.teacher.domain.entity.*;
import com.springjwt.module.teacher.domain.repository.SubjectRepository;
import com.springjwt.module.teacher.domain.repository.TeacherRepository;
import com.springjwt.module.teacher.domain.service.TeacherDomainService;
import com.springjwt.module.teacher.model.dto.*;
import com.springjwt.module.teacher.model.request.*;
import com.springjwt.module.user.domain.entity.Role;
import com.springjwt.module.user.domain.entity.User;
import com.springjwt.module.user.domain.repository.UserRepository;
import com.springjwt.module.user.domain.service.RoleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_ON_LEAVE = "on_leave";
    private static final String STATUS_PENDING = "pending";

    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherDomainService teacherDomainService;
    private final UserRepository userRepository;
    private final RoleDomainService roleDomainService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.temp-password}")
    private String tempPassword;

    @Override
    public Page<TeacherDto> listTeachers(TeacherListRequest request) {
        Sort sort = buildSort(request.getSortBy(), request.getSortDirection());
        PageRequest pageable = PageRequest.of(Math.max(request.getPage() - 1, 0), request.getSize(), sort);
        String search = normalizeSearch(request.getSearch());

        Page<Teacher> teachers = teacherRepository.search(request.getStatus(), search, pageable);
        List<TeacherDto> data = teachers.stream().map(this::toTeacherDto).toList();
        return new PageImpl<>(data, pageable, teachers.getTotalElements());
    }

    @Override
    public TeacherDto getTeacherById(Long id, Long currentUserId, boolean isAdmin) {
        Teacher teacher = teacherDomainService.getTeacherById(id);
        if (!isAdmin && !Objects.equals(teacher.getUser().getId(), currentUserId)) {
            throw new AppException("auth.forbidden", HttpStatus.FORBIDDEN);
        }
        return toTeacherDto(teacher);
    }

    @Override
    @Transactional
    public TeacherDto createTeacher(CreateTeacherRequest request) {
        validateUniqueUser(null, request.getEmail(), request.getPhone());
        teacherDomainService.validatePrimarySubject(request.getSubjectIds(), request.getPrimarySubjectId());
        List<Subject> subjects = teacherDomainService.getSubjectsOrThrow(request.getSubjectIds());

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                request.getPhone(),
                passwordEncoder.encode(tempPassword)
        );
        Role teacherRole = roleDomainService.findByName(ERole.ROLE_TEACHER);
        user.setRoles(Set.of(teacherRole));
        user = userRepository.save(user);

        Teacher teacher = Teacher.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .location(request.getLocation())
                .bio(request.getBio())
                .avatarUrl(request.getAvatarUrl())
                .status(STATUS_PENDING)
                .rating(BigDecimal.ZERO)
                .build();

        // Persist teacher first to guarantee a valid teacher_id for composite keys.
        teacher = teacherRepository.save(teacher);
        teacher.getTeacherSubjects().addAll(buildTeacherSubjects(teacher, subjects, request.getPrimarySubjectId()));

        teacher = teacherRepository.save(teacher);
        return toTeacherDto(teacher);
    }

    @Override
    @Transactional
    public TeacherDto updateTeacher(Long id, UpdateTeacherRequest request) {
        Teacher teacher = teacherDomainService.getTeacherById(id);

        validateUniqueUser(teacher.getUser().getId(), request.getEmail(), request.getPhone());
        teacherDomainService.validatePrimarySubject(request.getSubjectIds(), request.getPrimarySubjectId());
        List<Subject> subjects = teacherDomainService.getSubjectsOrThrow(request.getSubjectIds());

        User user = teacher.getUser();
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setGender(request.getGender());
        teacher.setDateOfBirth(request.getDateOfBirth());
        teacher.setLocation(request.getLocation());
        teacher.setBio(request.getBio());
        if (request.getAvatarUrl() != null) {
            teacher.setAvatarUrl(request.getAvatarUrl());
        }

        teacher.getTeacherSubjects().clear();
        teacher.getTeacherSubjects().addAll(buildTeacherSubjects(teacher, subjects, request.getPrimarySubjectId()));

        return toTeacherDto(teacherRepository.save(teacher));
    }

    @Override
    @Transactional
    public TeacherDto updateTeacherStatus(Long id, UpdateTeacherStatusRequest request) {
        Teacher teacher = teacherDomainService.getTeacherById(id);
        teacher.setStatus(request.getStatus());
        return toTeacherDto(teacherRepository.save(teacher));
    }

    @Override
    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = teacherDomainService.getTeacherById(id);
        User user = teacher.getUser();
        teacherRepository.delete(teacher);
        teacherRepository.flush();
        userRepository.delete(user);
    }

    @Override
    public TeacherStatusTabsDto getStatusTabs() {
        long active = teacherRepository.countByStatus(STATUS_ACTIVE);
        long onLeave = teacherRepository.countByStatus(STATUS_ON_LEAVE);
        long pending = teacherRepository.countByStatus(STATUS_PENDING);
        return TeacherStatusTabsDto.builder()
                .active(active)
                .onLeave(onLeave)
                .pending(pending)
                .total(active + onLeave + pending)
                .build();
    }

    @Override
    public List<TeacherOptionDto> getTeacherOptions(String status) {
        List<Teacher> teachers = status == null || status.isBlank()
                ? teacherRepository.findAllByOrderByFirstNameAscLastNameAsc()
                : teacherRepository.findByStatusOrderByFirstNameAscLastNameAsc(status);

        return teachers.stream()
                .map(t -> TeacherOptionDto.builder()
                        .id(t.getId())
                        .fullName((t.getFirstName() + " " + t.getLastName()).trim())
                        .email(t.getUser().getEmail())
                        .build())
                .toList();
    }

    @Override
    public List<SubjectDto> getSubjects() {
        return subjectRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(subject -> SubjectDto.builder().id(subject.getId()).name(subject.getName()).build())
                .toList();
    }

    private Set<TeacherSubject> buildTeacherSubjects(Teacher teacher, List<Subject> subjects, Long primarySubjectId) {
        Set<TeacherSubject> links = new HashSet<>();
        for (Subject subject : subjects) {
            TeacherSubject link = TeacherSubject.builder()
                    .id(new TeacherSubjectId(teacher.getId(), subject.getId()))
                    .teacher(teacher)
                    .subject(subject)
                    .primary(Objects.equals(subject.getId(), primarySubjectId))
                    .build();
            links.add(link);
        }
        return links;
    }

    private void validateUniqueUser(Long userId, String email, String phone) {
        boolean emailExists = userId == null
                ? userRepository.existsByEmail(email)
                : userRepository.existsByEmailAndIdNot(email, userId);
        if (emailExists) {
            throw new AppException("teacher.email.exists", HttpStatus.CONFLICT);
        }

        boolean phoneExists = userId == null
                ? userRepository.existsByPhone(phone)
                : userRepository.existsByPhoneAndIdNot(phone, userId);
        if (phoneExists) {
            throw new AppException("teacher.phone.exists", HttpStatus.CONFLICT);
        }
    }

    private TeacherDto toTeacherDto(Teacher teacher) {
        List<TeacherSubjectDto> subjects = teacher.getTeacherSubjects().stream()
                .map(ts -> TeacherSubjectDto.builder()
                        .id(ts.getSubject().getId())
                        .name(ts.getSubject().getName())
                        .isPrimary(ts.isPrimary())
                        .build())
                .sorted(Comparator.comparing(TeacherSubjectDto::isPrimary).reversed()
                        .thenComparing(TeacherSubjectDto::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        String firstName = Optional.ofNullable(teacher.getFirstName()).orElse("").trim();
        String lastName = Optional.ofNullable(teacher.getLastName()).orElse("").trim();
        String fullName = (firstName + " " + lastName).trim();

        return TeacherDto.builder()
                .id(teacher.getId())
                .userId(teacher.getUser().getId())
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .fullName(fullName)
                .initials(buildInitials(firstName, lastName))
                .email(teacher.getUser().getEmail())
                .phone(teacher.getUser().getPhone())
                .gender(teacher.getGender())
                .dateOfBirth(teacher.getDateOfBirth())
                .avatarUrl(teacher.getAvatarUrl())
                .location(teacher.getLocation())
                .bio(teacher.getBio())
                .status(teacher.getStatus())
                .rating(teacher.getRating())
                .classCount(0)
                .studentCount(0)
                .subjects(subjects)
                .createdAt(teacher.getCreatedAt())
                .updatedAt(teacher.getUpdatedAt())
                .build();
    }

    private String buildInitials(String firstName, String lastName) {
        StringBuilder result = new StringBuilder();
        if (!firstName.isBlank()) {
            result.append(Character.toUpperCase(firstName.charAt(0)));
        }
        if (!lastName.isBlank()) {
            result.append(Character.toUpperCase(lastName.charAt(0)));
        }
        return result.toString();
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isBlank() || "createdAt".equals(sortBy)) {
            return Sort.by("asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC, "createdAt");
        }

        String mappedSortField = switch (sortBy) {
            case "firstName" -> "firstName";
            case "lastName" -> "lastName";
            case "status" -> "status";
            case "updatedAt" -> "updatedAt";
            default -> throw new AppException("teacher.validation.sortBy.invalid", HttpStatus.BAD_REQUEST);
        };

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, mappedSortField);
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return null;
        }
        String value = search.trim();
        return value.isEmpty() ? null : value;
    }
}

