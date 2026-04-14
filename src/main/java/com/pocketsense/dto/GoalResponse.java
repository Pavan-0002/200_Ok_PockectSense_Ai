package com.pocketsense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class GoalResponse {
    private Double targetAmount;
    private LocalDate deadline;
    private Double totalSaved;
    private Double remainingAmount;
    private Double dailySavingsNeeded;
    private Long daysRemaining;
    private Double progress;
    private String status;
}
