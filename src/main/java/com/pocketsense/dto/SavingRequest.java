package com.pocketsense.dto;

import lombok.Data;
import java.util.UUID;
import java.time.LocalDate;

@Data
public class SavingRequest {
    private UUID userId;
    private Double amount;
    private LocalDate date;
}
