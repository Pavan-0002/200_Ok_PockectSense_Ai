package com.pocketsense.controller;

import com.pocketsense.dto.AnalyticsResponse;
import com.pocketsense.dto.BudgetResponse;
import com.pocketsense.dto.ProfileRequest;
import com.pocketsense.model.Expense;
import com.pocketsense.model.Profile;
import com.pocketsense.repository.ExpenseRepository;
import com.pocketsense.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @PostMapping("/profile")
    public ResponseEntity<Profile> saveProfile(@RequestBody ProfileRequest request) {
        Profile profile = profileService.saveOrUpdateProfile(request);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<Profile> getProfile(@PathVariable UUID userId) {
        Profile profile = profileService.getProfileByUserId(userId);
        if(profile != null) {
            return ResponseEntity.ok(profile);
        } else {
            Profile emptyProfile = new Profile();
            emptyProfile.setUserId(userId);
            emptyProfile.setName("New User");
            emptyProfile.setMonthlyBudget(5000.0);
            emptyProfile.setSavingsGoal(20000.0);
            return ResponseEntity.ok(emptyProfile);
        }
    }

    @GetMapping("/budget/{userId}")
    public ResponseEntity<BudgetResponse> getBudget(@PathVariable UUID userId) {
        Profile profile = profileService.getProfileByUserId(userId);
        double monthlyBudget = (profile != null && profile.getMonthlyBudget() != null) ? profile.getMonthlyBudget() : 5000.0;
        
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        double spent = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double remaining = monthlyBudget - spent;
        
        String status = "safe";
        if (spent > monthlyBudget) {
            status = "danger";
        } else if (spent > (monthlyBudget * 0.8)) {
            status = "warning";
        }

        return ResponseEntity.ok(new BudgetResponse(monthlyBudget, spent, remaining, status));
    }

    @GetMapping("/analytics/{userId}")
    public ResponseEntity<AnalyticsResponse> getAnalytics(@PathVariable UUID userId) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        double totalSpending = expenses.stream().mapToDouble(Expense::getAmount).sum();
        
        String topCategory = "None";
        if (!expenses.isEmpty()) {
            topCategory = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
        }
        
        double avgDaily = expenses.isEmpty() ? 0.0 : totalSpending / Math.max(1, expenses.size()); // Basic estimation
        
        return ResponseEntity.ok(new AnalyticsResponse(totalSpending, topCategory, avgDaily));
    }
}
