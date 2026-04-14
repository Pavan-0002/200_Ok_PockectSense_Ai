package com.pocketsense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnalyticsResponse {
    private Double totalSpending;
    private String topCategory;
    private Double avgDaily;
}
