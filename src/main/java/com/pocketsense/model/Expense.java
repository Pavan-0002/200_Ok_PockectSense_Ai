package com.pocketsense.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String category; // food, travel, entertainment, study, other

    @Column(name = "is_regret")
    private Boolean isRegret = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
