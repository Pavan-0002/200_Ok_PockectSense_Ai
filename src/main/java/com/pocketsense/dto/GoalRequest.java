package com.pocketsense.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class GoalRequest {
    private UUID userId;
    private Double targetAmount;
    private LocalDate deadline;
}
