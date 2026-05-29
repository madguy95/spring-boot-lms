package com.springjwt.module.enrollment.business;

import com.springjwt.module.enrollment.model.dto.BulkActionResultDto;
import com.springjwt.module.enrollment.model.dto.EnrollmentDto;
import com.springjwt.module.enrollment.model.dto.EnrollmentStatusTabsDto;
import com.springjwt.module.enrollment.model.request.ApproveEnrollmentRequest;
import com.springjwt.module.enrollment.model.request.BulkActionRequest;
import com.springjwt.module.enrollment.model.request.CreateEnrollmentRequest;
import com.springjwt.module.enrollment.model.request.EnrollmentListRequest;
import com.springjwt.module.enrollment.model.request.RejectEnrollmentRequest;
import com.springjwt.module.enrollment.model.request.UpdatePaymentRequest;
import com.springjwt.module.enrollment.model.request.WorkshopSignupRequest;
import org.springframework.data.domain.Page;

public interface EnrollmentService {

    Page<EnrollmentDto> listEnrollments(EnrollmentListRequest request);

    EnrollmentDto getEnrollmentById(Long id);

    EnrollmentDto createEnrollment(CreateEnrollmentRequest request);

    /**
     * Guest workshop signup — no auth, called from the public blog detail page.
     * Derives requestedCourseId from the chosen class and pins
     * channel='website_workshop' so admins can triage.
     */
    EnrollmentDto createWorkshopSignup(WorkshopSignupRequest request);

    EnrollmentDto approveEnrollment(Long id, ApproveEnrollmentRequest request);

    EnrollmentDto waitlistEnrollment(Long id);

    EnrollmentDto rejectEnrollment(Long id, RejectEnrollmentRequest request);

    EnrollmentDto updatePayment(Long id, UpdatePaymentRequest request);

    EnrollmentStatusTabsDto getStatusTabs();

    BulkActionResultDto bulkAction(BulkActionRequest request);
}
