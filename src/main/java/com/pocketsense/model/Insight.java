package com.pocketsense.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "insights")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Insight {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "personality_type")
    private String personalityType;

    @Column(name = "avg_spending")
    private Double avgSpending;
}
