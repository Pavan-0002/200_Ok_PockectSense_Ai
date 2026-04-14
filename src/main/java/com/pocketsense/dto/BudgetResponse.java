package com.pocketsense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BudgetResponse {
    private Double budget;
    private Double spent;
    private Double remaining;
    private String status; // "safe" or "warning" or "danger"
}
