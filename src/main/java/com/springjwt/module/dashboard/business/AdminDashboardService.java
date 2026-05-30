package com.springjwt.module.dashboard.business;

import com.springjwt.module.dashboard.model.dto.DashboardSummaryDto;
import com.springjwt.module.dashboard.model.request.DashboardSummaryRequest;

public interface AdminDashboardService {
    DashboardSummaryDto getSummary(DashboardSummaryRequest request);
}
