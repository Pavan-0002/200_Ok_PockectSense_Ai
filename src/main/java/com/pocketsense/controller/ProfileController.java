package com.pocketsense.controller;

import com.pocketsense.dto.AnalyticsResponse;
import com.pocketsense.dto.BudgetResponse;
import com.pocketsense.dto.ProfileRequest;
import com.pocketsense.model.Expense;
import com.pocketsense.model.Profile;
import com.pocketsense.repository.ExpenseRepository;
import com.pocketsense.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @PostMapping("/profile")
    public ResponseEntity<?> saveProfile(@RequestBody ProfileRequest request) {
        try {
            if (request == null || request.getUserId() == null) {
                return ResponseEntity.badRequest().body("Profile data and User ID are required");
            }
            Profile profile = profileService.saveOrUpdateProfile(request);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Error saving profile: ", e);
            return ResponseEntity.internalServerError().body("Error updating profile");
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable UUID userId) {
        try {
            if (userId == null) return ResponseEntity.badRequest().body("User ID is required");
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
        } catch (Exception e) {
            logger.error("Error fetching profile for user {}: ", userId, e);
            return ResponseEntity.internalServerError().body("Error fetching profile data");
        }
    }

    @GetMapping("/budget/{userId}")
    public ResponseEntity<?> getBudget(@PathVariable UUID userId) {
        try {
            if (userId == null) return ResponseEntity.badRequest().body("User ID is required");
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
        } catch (Exception e) {
            logger.error("Error fetching budget for user {}: ", userId, e);
            return ResponseEntity.internalServerError().body("Error calculating budget metrics");
        }
    }

    @GetMapping("/analytics/{userId}")
    public ResponseEntity<?> getAnalytics(@PathVariable UUID userId) {
        try {
            if (userId == null) return ResponseEntity.badRequest().body("User ID is required");
            List<Expense> expenses = expenseRepository.findByUserId(userId);
            double totalSpending = expenses.stream().mapToDouble(Expense::getAmount).sum();
            
            String topCategory = "None";
            if (!expenses.isEmpty()) {
                topCategory = expenses.stream()
                    .collect(Collectors.groupingBy(e -> e.getCategory() != null ? e.getCategory() : "Other", Collectors.summingDouble(Expense::getAmount)))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("None");
            }
            
            double avgDaily = expenses.isEmpty() ? 0.0 : totalSpending / Math.max(1, expenses.size());
            return ResponseEntity.ok(new AnalyticsResponse(totalSpending, topCategory, avgDaily));
        } catch (Exception e) {
            logger.error("Error fetching analytics for user {}: ", userId, e);
            return ResponseEntity.internalServerError().body("Error generating spending analytics");
        }
    }
}
