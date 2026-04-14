package com.pocketsense.controller;

import com.pocketsense.dto.ExpenseRequest;
import com.pocketsense.model.Expense;
import com.pocketsense.service.ExpenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    @Autowired
    private ExpenseService expenseService;

    @PostMapping("/addExpense")
    public ResponseEntity<?> addExpense(@RequestBody ExpenseRequest request) {
        try {
            if (request == null || request.getUserId() == null || request.getAmount() == null) {
                return ResponseEntity.badRequest().body("Invalid expense data");
            }
            Expense expense = expenseService.saveExpense(request);
            return ResponseEntity.ok(expense);
        } catch (Exception e) {
            logger.error("Error adding expense: ", e);
            return ResponseEntity.internalServerError().body("Failed to log expense");
        }
    }

    @GetMapping("/expenses/{userId}")
    public ResponseEntity<?> getExpenses(@PathVariable UUID userId) {
        try {
            if (userId == null) return ResponseEntity.badRequest().body("User ID required");
            List<Expense> expenses = expenseService.getExpensesByUserId(userId);
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            logger.error("Error fetching expenses for user {}: ", userId, e);
            return ResponseEntity.internalServerError().body("Error fetching transactions");
        }
    }

    @DeleteMapping("/expense/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable String id) {
        try {
            expenseService.deleteExpense(UUID.fromString(id));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting expense {}: ", id, e);
            return ResponseEntity.internalServerError().body("Deletion failed");
        }
    }

    @DeleteMapping("/reset/{userId}")
    public ResponseEntity<?> resetData(@PathVariable UUID userId) {
        try {
            if (userId == null) return ResponseEntity.badRequest().body("User ID required");
            expenseService.resetUserData(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error resetting data for user {}: ", userId, e);
            return ResponseEntity.internalServerError().body("Reset failed");
        }
    }
}
