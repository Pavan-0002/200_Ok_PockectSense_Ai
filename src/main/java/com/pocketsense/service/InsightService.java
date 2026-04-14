package com.pocketsense.service;

import com.pocketsense.dto.AlertResponse;
import com.pocketsense.dto.InsightResponse;
import com.pocketsense.model.Expense;
import com.pocketsense.model.Profile;
import com.pocketsense.repository.ExpenseRepository;
import com.pocketsense.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InsightService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ProfileRepository profileRepository;

    public InsightResponse getInsights(UUID userId) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);

        if (expenses.isEmpty()) {
            return new InsightResponse("New Spender", "None", "Stable", "Start logging expenses to generate AI insights.", new ArrayList<>());
        }

        double totalSpending = expenses.stream().mapToDouble(Expense::getAmount).sum();
        
        Map<String, Double> categorySpending = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));
        
        String topCategory = categorySpending.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("None");
        
        double weekendSpending = expenses.stream()
                .filter(e -> {
                    if (e.getCreatedAt() != null) {
                        DayOfWeek day = e.getCreatedAt().getDayOfWeek();
                        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
                    }
                    return false;
                })
                .mapToDouble(Expense::getAmount).sum();

        String personality = "Balanced Spender";
        if (totalSpending > 0 && ("food".equalsIgnoreCase(topCategory) || "restaurant".equalsIgnoreCase(topCategory))) {
            personality = "Foodie Spender";
        } else if (totalSpending > 0 && (weekendSpending / totalSpending) > 0.40) {
            personality = "Weekend Spender";
        }

        String trend = "Increasing";
        String message = "Your spending patterns indicate a high concentration in " + topCategory + ".";

        List<String> badges = new ArrayList<>();
        badges.add("Starter Log"); // Everyone gets this
        
        Profile profile = profileRepository.findByUserId(userId).orElse(null);
        if (profile != null && profile.getMonthlyBudget() != null) {
            if (totalSpending < profile.getMonthlyBudget() * 0.5) {
                badges.add("Budget Boss");
            }
        }
        if (expenses.size() > 10) {
            badges.add("Consistent Tracker");
        }
        if (personality.equals("Saver")) {
            badges.add("Savings Champion");
        } else if (personality.equals("Balanced Spender")) {
            badges.add("Balance Master");
        }

        return new InsightResponse(personality, topCategory, trend, message, badges);
    }

    public List<AlertResponse> getAlerts(UUID userId) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        List<AlertResponse> alerts = new ArrayList<>();

        if (expenses.isEmpty()) return alerts;

        Map<String, DoubleSummaryStatistics> categoryStats = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summarizingDouble(Expense::getAmount)
                ));

        for (Expense expense : expenses) {
            DoubleSummaryStatistics stats = categoryStats.get(expense.getCategory());
            double average = stats.getAverage();
            
            if (average > 0 && expense.getAmount() > (2 * average) && stats.getCount() > 1) {
                alerts.add(new AlertResponse(
                        "warning",
                        "Unusual spending: " + expense.getAmount() + " on " + expense.getCategory(),
                        expense.getCategory(),
                        expense.getAmount(),
                        average
                ));
            }
        }

        // Check budget logic
        Profile profile = profileRepository.findByUserId(userId).orElse(null);
        if(profile != null && profile.getMonthlyBudget() != null) {
            double monthlyBudget = profile.getMonthlyBudget();
            double totalSpent = expenses.stream().mapToDouble(Expense::getAmount).sum();
            
            if(totalSpent > monthlyBudget) {
                alerts.add(0, new AlertResponse("danger", "You have exceeded your monthly budget!", "General", totalSpent, monthlyBudget));
            } else if(totalSpent > monthlyBudget * 0.8) {
                alerts.add(0, new AlertResponse("warning", "You have spent over 80% of your budget.", "General", totalSpent, monthlyBudget));
            } else {
                alerts.add(0, new AlertResponse("tips", "You are within your safe budget limits.", "General", totalSpent, monthlyBudget));
            }
        }

        return alerts.stream().limit(5).collect(Collectors.toList());
    }

    public com.pocketsense.dto.HealthResponse getHealthScore(UUID userId) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        Profile profile = profileRepository.findByUserId(userId).orElse(null);

        if (expenses.isEmpty()) {
            return new com.pocketsense.dto.HealthResponse(100, "Excellent", "Start tracking expenses to keep up the good work.");
        }

        double monthlyBudget = (profile != null && profile.getMonthlyBudget() != null) ? profile.getMonthlyBudget() : 5000.0;
        double totalSpent = expenses.stream().mapToDouble(Expense::getAmount).sum();
        long regretCount = expenses.stream().filter(e -> Boolean.TRUE.equals(e.getIsRegret())).count();
        double regretPercentage = expenses.isEmpty() ? 0 : ((double) regretCount / expenses.size()) * 100;

        int score = 100;
        
        // Penalize for overspending
        if (totalSpent > monthlyBudget) score -= 30;
        else if (totalSpent > monthlyBudget * 0.8) score -= 10;
        else score += 5; // Reward safe spending
        
        // Penalize for high regret
        if (regretPercentage > 50) score -= 20;
        else if (regretPercentage > 20) score -= 10;

        score = Math.max(0, Math.min(100, score)); // Clamp between 0-100
        
        String status = score >= 80 ? "Good" : (score >= 50 ? "Average" : "Poor");
        String message = score >= 80 ? "You are managing your finances well." : "Watch your spending habits.";

        return new com.pocketsense.dto.HealthResponse(score, status, message);
    }

    public com.pocketsense.dto.PredictionResponse getPrediction(UUID userId) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        Profile profile = profileRepository.findByUserId(userId).orElse(null);
        double monthlyBudget = (profile != null && profile.getMonthlyBudget() != null) ? profile.getMonthlyBudget() : 5000.0;

        if (expenses.isEmpty()) {
            return new com.pocketsense.dto.PredictionResponse(0.0, monthlyBudget, "Not enough data to predict.");
        }

        double totalSpent = expenses.stream().mapToDouble(Expense::getAmount).sum();
        
        // Simple prediction: daily avg over last N days * 30.
        // Assuming current expenses span over the days tracked.
        long count = expenses.size();
        double dailyAvg = totalSpent / Math.max(1, count); // VERY simplified, assuming 1 expense per day average
        double predictedTotal = dailyAvg * 30; // 30 days month
        double expectedSavings = Math.max(0, monthlyBudget - predictedTotal);

        String message = predictedTotal > monthlyBudget ? "⚠️ You will overspend this month" : "✅ You are on track";
        return new com.pocketsense.dto.PredictionResponse(Math.round(predictedTotal), Math.round(expectedSavings), message);
    }

    public com.pocketsense.dto.RegretResponse getRegret(UUID userId) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        if (expenses.isEmpty()) {
            return new com.pocketsense.dto.RegretResponse(0, "No expenses to analyze.");
        }

        long totalCount = expenses.size();
        long regretCount = expenses.stream().filter(e -> Boolean.TRUE.equals(e.getIsRegret())).count();

        int percentage = (int) (((double) regretCount / totalCount) * 100);
        
        String topRegretCategory = expenses.stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsRegret()))
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");

        String message = topRegretCategory.equals("None") ? "You have no regret expenses." : "😬 You regret most " + topRegretCategory + " expenses.";

        return new com.pocketsense.dto.RegretResponse(percentage, message);
    }
}
