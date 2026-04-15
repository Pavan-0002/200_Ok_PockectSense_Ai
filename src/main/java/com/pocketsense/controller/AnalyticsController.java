package com.pocketsense.controller;

import com.pocketsense.dto.AnalyticsResponse;
import com.pocketsense.model.Expense;
import com.pocketsense.repository.ExpenseRepository;
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
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    @Autowired
    private ExpenseRepository expenseRepository;

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
