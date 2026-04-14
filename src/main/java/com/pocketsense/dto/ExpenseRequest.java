package com.pocketsense.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ExpenseRequest {
    private UUID userId;
    private Double amount;
    private String category;
    private Boolean isRegret;
}
