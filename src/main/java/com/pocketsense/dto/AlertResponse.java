package com.pocketsense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlertResponse {
    private String type; // e.g., danger, warning, tips
    private String message;
    private String category;
    private Double currentExpense;
    private Double referenceValue;
}
