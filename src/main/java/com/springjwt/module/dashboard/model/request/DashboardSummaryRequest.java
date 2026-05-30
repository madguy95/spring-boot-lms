package com.springjwt.module.dashboard.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardSummaryRequest {
    /** week | month | term — used to size the "delta" window. Defaults to week. */
    private String period;
}
