package com.pocketsense.controller;

import com.pocketsense.dto.BudgetResponse;
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
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class BudgetController {

    private static final Logger logger = LoggerFactory.getLogger(BudgetController.class);

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ExpenseRepository expenseRepository;

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
}
