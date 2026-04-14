package com.pocketsense.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ProfileRequest {
    private UUID userId;
    private String name;
    private String email;
    private Double monthlyBudget;
    private Double savingsGoal;
    private String imageUrl;
}
