package com.springjwt.module.consultation.business.impl;

import com.springjwt.module.consultation.business.ConsultationRequestService;
import com.springjwt.module.consultation.domain.entity.ConsultationRequest;
import com.springjwt.module.consultation.domain.repository.ConsultationRequestRepository;
import com.springjwt.module.consultation.domain.service.ConsultationRequestDomainService;
import com.springjwt.module.consultation.model.dto.ConsultationRequestDto;
import com.springjwt.module.consultation.model.dto.ConsultationStatusTabsDto;
import com.springjwt.module.consultation.model.request.ConsultationListRequest;
import com.springjwt.module.consultation.model.request.SubmitConsultationRequest;
import com.springjwt.module.consultation.model.request.UpdateConsultationStatusRequest;
import com.springjwt.module.course.domain.entity.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationRequestServiceImpl implements ConsultationRequestService {

    private static final String STATUS_NEW = "new";

    private final ConsultationRequestRepository consultationRequestRepository;
    private final ConsultationRequestDomainService consultationRequestDomainService;

    @Override
    public Page<ConsultationRequestDto> listConsultationRequests(ConsultationListRequest request) {
        Sort sort = buildSort(request.getSortBy(), request.getSortDirection());
        PageRequest pageable = PageRequest.of(Math.max(request.getPage() - 1, 0), request.getSize(), sort);
        String search = normalize(request.getSearch());

        Page<ConsultationRequest> page = consultationRequestRepository.search(
                request.getStatus(), search, pageable);
        List<ConsultationRequestDto> data = page.stream().map(this::toDto).toList();
        return new PageImpl<>(data, pageable, page.getTotalElements());
    }

    @Override
    public ConsultationRequestDto getById(Long id) {
        return toDto(consultationRequestDomainService.getByIdOrThrow(id));
    }

    @Override
    @Transactional
    public ConsultationRequestDto submitPublic(SubmitConsultationRequest request) {
        Course course = consultationRequestDomainService.getCourseOrNull(request.getInterestedCourseId());

        ConsultationRequest entity = ConsultationRequest.builder()
                .parentName(request.getParentName().trim())
                .parentPhone(request.getParentPhone().trim())
                .childName(normalize(request.getChildName()))
                .interestedCourse(course)
                .note(normalize(request.getNote()))
                .status(STATUS_NEW)
                .build();

        return toDto(consultationRequestRepository.save(entity));
    }

    @Override
    @Transactional
    public ConsultationRequestDto updateStatus(Long id, UpdateConsultationStatusRequest request) {
        ConsultationRequest entity = consultationRequestDomainService.getByIdOrThrow(id);
        entity.setStatus(request.getStatus());
        return toDto(consultationRequestRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ConsultationRequest entity = consultationRequestDomainService.getByIdOrThrow(id);
        consultationRequestRepository.delete(entity);
    }

    @Override
    public ConsultationStatusTabsDto getStatusTabs() {
        long newCount = consultationRequestRepository.countByStatus("new");
        long contacted = consultationRequestRepository.countByStatus("contacted");
        long enrolled = consultationRequestRepository.countByStatus("enrolled");
        long notInterested = consultationRequestRepository.countByStatus("not_interested");
        return ConsultationStatusTabsDto.builder()
                .all(newCount + contacted + enrolled + notInterested)
                .newCount(newCount)
                .contacted(contacted)
                .enrolled(enrolled)
                .notInterested(notInterested)
                .build();
    }

    // ----- helpers -----

    private ConsultationRequestDto toDto(ConsultationRequest e) {
        Course course = e.getInterestedCourse();
        return ConsultationRequestDto.builder()
                .id(e.getId())
                .parentName(e.getParentName())
                .parentPhone(e.getParentPhone())
                .childName(e.getChildName())
                .interestedCourseId(course == null ? null : course.getId())
                .interestedCourseTitle(course == null ? null : course.getTitle())
                .note(e.getNote())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private Sort buildSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String field = (sortBy == null || sortBy.isBlank()) ? "createdAt" : switch (sortBy) {
            case "createdAt", "updatedAt", "status", "parentName" -> sortBy;
            default -> "createdAt";
        };
        return Sort.by(direction, field);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }
}
