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

        System.out.println("Processing Insights for user: " + userId + " | Total Expenses: " + expenses.size());

        double totalSpending = expenses.stream().mapToDouble(Expense::getAmount).sum();
        
        Map<String, Double> categorySpending = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().toLowerCase(),
                        Collectors.summingDouble(Expense::getAmount)
                ));
        
        String topCategory = categorySpending.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("None");
        
        double foodSpending = categorySpending.getOrDefault("food", 0.0) + categorySpending.getOrDefault("restaurant", 0.0) + categorySpending.getOrDefault("dining", 0.0);
        double foodPercentage = totalSpending > 0 ? (foodSpending / totalSpending) * 100 : 0;

        double weekendSpending = 0;
        double weekdaySpending = 0;
        for (Expense e : expenses) {
            if (e.getCreatedAt() != null) {
                DayOfWeek day = e.getCreatedAt().getDayOfWeek();
                if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) weekendSpending += e.getAmount();
                else weekdaySpending += e.getAmount();
            }
        }

        String personality = "Balanced Spender";
        if (foodPercentage > 40) {
            personality = "Foodie Spender";
        } else if (weekendSpending > weekdaySpending && totalSpending > 0) {
            personality = "Weekend Spender";
        }

        String trend = "Stable";
        String message = "Your spending patterns indicate a high concentration in " + topCategory + ".";

        List<String> badges = new ArrayList<>();
        badges.add("Starter Log"); 
        
        Profile profile = profileRepository.findByUserId(userId).orElse(null);
        double monthlyBudget = (profile != null && profile.getMonthlyBudget() != null) ? profile.getMonthlyBudget() : 5000.0;

        if (totalSpending < monthlyBudget * 0.5) badges.add("Budget Boss");
        if (expenses.size() > 10) badges.add("Consistent Tracker");
        if (personality.contains("Foodie")) badges.add("Culinary Enthusiast");

        return new InsightResponse(personality, topCategory, trend, message, badges);
    }

    public List<AlertResponse> getAlerts(UUID userId) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        List<AlertResponse> alerts = new ArrayList<>();
        if (expenses.isEmpty()) return alerts;

        Map<String, DoubleSummaryStatistics> categoryStats = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().toLowerCase(),
                        Collectors.summarizingDouble(Expense::getAmount)
                ));

        // Detect 2x Average Anomalies for recent expenses
        for (int i = Math.max(0, expenses.size() - 5); i < expenses.size(); i++) {
            Expense expense = expenses.get(i);
            DoubleSummaryStatistics stats = categoryStats.get(expense.getCategory().toLowerCase());
            double average = stats.getAverage();
            
            if (stats.getCount() > 1 && expense.getAmount() > (2 * average)) {
                alerts.add(new AlertResponse(
                        "danger",
                        "⚠️ Unusual spending: ₹" + expense.getAmount() + " on " + expense.getCategory(),
                        expense.getCategory(),
                        expense.getAmount(),
                        average
                ));
            }
        }

        Profile profile = profileRepository.findByUserId(userId).orElse(null);
        if(profile != null && profile.getMonthlyBudget() != null) {
            double budget = profile.getMonthlyBudget();
            double totalSpent = expenses.stream().mapToDouble(Expense::getAmount).sum();
            
            if(totalSpent > budget) {
                alerts.add(0, new AlertResponse("danger", "🚨 CRITICAL: Budget Exceeded!", "Financial Health", totalSpent, budget));
            } else if(totalSpent > budget * 0.8) {
                alerts.add(0, new AlertResponse("warning", "⚠️ Caution: Over 80% budget used.", "Warning", totalSpent, budget));
            }
        }

        return alerts.stream().limit(5).collect(Collectors.toList());
    }

    public com.pocketsense.dto.HealthResponse getHealthScore(UUID userId) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        Profile profile = profileRepository.findByUserId(userId).orElse(null);
        double monthlyBudget = (profile != null && profile.getMonthlyBudget() != null) ? profile.getMonthlyBudget() : 5000.0;
        double totalSpent = expenses.stream().mapToDouble(Expense::getAmount).sum();
        
        long regretCount = expenses.stream().filter(e -> Boolean.TRUE.equals(e.getIsRegret())).count();
        double regretPercentage = expenses.isEmpty() ? 0 : ((double) regretCount / expenses.size()) * 100;

        // score = 100 - overspending - regret%
        double overspendAmt = Math.max(0, totalSpent - monthlyBudget);
        double overspendPenalty = (overspendAmt / monthlyBudget) * 50; // Normalize overspend
        
        int score = (int) (100 - Math.min(50, overspendPenalty) - Math.min(50, regretPercentage));
        score = Math.max(0, Math.min(100, score));
        
        String status = score >= 80 ? "Pristine ✅" : (score >= 50 ? "Stable ⚠️" : "Critical 🚨");
        String message = score >= 80 ? "Excellent financial management." : "Optimization required in spending habits.";

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
