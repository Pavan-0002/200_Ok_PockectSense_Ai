package com.pocketsense.controller;

import com.pocketsense.dto.ExpenseRequest;
import com.pocketsense.model.Expense;
import com.pocketsense.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping("/addExpense")
    public ResponseEntity<Expense> addExpense(@RequestBody ExpenseRequest request) {
        Expense expense = expenseService.saveExpense(request);
        return ResponseEntity.ok(expense);
    }

    @GetMapping("/expenses/{userId}")
    public ResponseEntity<List<Expense>> getExpenses(@PathVariable UUID userId) {
        List<Expense> expenses = expenseService.getExpensesByUserId(userId);
        return ResponseEntity.ok(expenses);
    }

    @DeleteMapping("/expense/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable String id) {
        expenseService.deleteExpense(UUID.fromString(id));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reset/{userId}")
    public ResponseEntity<Void> resetData(@PathVariable UUID userId) {
        expenseService.resetUserData(userId);
        return ResponseEntity.ok().build();
    }
}
