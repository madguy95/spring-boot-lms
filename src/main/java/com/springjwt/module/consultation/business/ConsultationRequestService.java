package com.springjwt.module.consultation.business;

import com.springjwt.module.consultation.model.dto.ConsultationRequestDto;
import com.springjwt.module.consultation.model.dto.ConsultationStatusTabsDto;
import com.springjwt.module.consultation.model.request.ConsultationListRequest;
import com.springjwt.module.consultation.model.request.SubmitConsultationRequest;
import com.springjwt.module.consultation.model.request.UpdateConsultationStatusRequest;
import org.springframework.data.domain.Page;

public interface ConsultationRequestService {

    Page<ConsultationRequestDto> listConsultationRequests(ConsultationListRequest request);

    ConsultationRequestDto getById(Long id);

    ConsultationRequestDto submitPublic(SubmitConsultationRequest request);

    ConsultationRequestDto updateStatus(Long id, UpdateConsultationStatusRequest request);

    void delete(Long id);

    ConsultationStatusTabsDto getStatusTabs();
}
