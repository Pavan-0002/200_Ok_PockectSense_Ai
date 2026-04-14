package com.pocketsense.service;

import com.pocketsense.dto.GoalRequest;
import com.pocketsense.dto.GoalResponse;
import com.pocketsense.model.Expense;
import com.pocketsense.model.Goal;
import com.pocketsense.model.Profile;
import com.pocketsense.repository.ExpenseRepository;
import com.pocketsense.repository.GoalRepository;
import com.pocketsense.repository.ProfileRepository;
import com.pocketsense.repository.SavingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private SavingRepository savingRepository;

    public Goal setGoal(GoalRequest request) {
        Goal goal = new Goal();
        goal.setUserId(request.getUserId());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setDeadline(request.getDeadline());
        return goalRepository.save(goal);
    }

    public GoalResponse getGoalResponse(UUID userId) {
        List<Goal> goals = goalRepository.findAllByUserId(userId);
        if (goals.isEmpty()) {
            return new GoalResponse(0.0, LocalDate.now(), 0.0, 0.0, 0L, 0.0, "No active goal ❌");
        }
        Goal goal = goals.get(goals.size() - 1); // Get latest goal

        List<com.pocketsense.model.Saving> savings = savingRepository.findByUserId(userId);
        double totalSaved = savings.stream().mapToDouble(com.pocketsense.model.Saving::getAmount).sum();
        double remainingAmount = Math.max(0, goal.getTargetAmount() - totalSaved);
        
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), goal.getDeadline());
        if (daysRemaining < 0) daysRemaining = 0;

        // Progress tracking:
        double progress = 0.0;
        if(goal.getTargetAmount() > 0) {
            progress = (totalSaved / goal.getTargetAmount()) * 100;
        }

        double dailySavingsNeeded = 0.0;
        if (daysRemaining > 0) {
            dailySavingsNeeded = remainingAmount / daysRemaining;
        }
        
        String status = "On Track ✅";
        if(progress < (100.0 * (ChronoUnit.DAYS.between(goal.getCreatedAt() != null ? goal.getCreatedAt().toLocalDate() : LocalDate.now().minusDays(30), LocalDate.now()) / Math.max(1, ChronoUnit.DAYS.between(goal.getCreatedAt() != null ? goal.getCreatedAt().toLocalDate() : LocalDate.now().minusDays(30), goal.getDeadline()))))) {
             // simplified: if progress is less than time elapsed, or just use a simple threshold
             if (progress < 20 && daysRemaining < 60) status = "Behind ⚠️";
        }
        
        // Simple "Behind" logic: if amount remaining > 0 and days == 0, or if daily needed is too high
        if (remainingAmount > 0 && daysRemaining == 0) status = "Behind ⚠️";
        if (dailySavingsNeeded > 1000) status = "Behind ⚠️"; // Just a mock threshold for "Behind"

        return new GoalResponse(
                goal.getTargetAmount(),
                goal.getDeadline(),
                totalSaved,
                remainingAmount,
                dailySavingsNeeded,
                daysRemaining,
                Math.min(progress, 100.0),
                status
        );
    }
}
