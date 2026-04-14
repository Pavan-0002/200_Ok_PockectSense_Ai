package com.pocketsense.service;

import com.pocketsense.dto.ExpenseRequest;
import com.pocketsense.model.Expense;
import com.pocketsense.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    public Expense saveExpense(ExpenseRequest request) {
        Expense expense = new Expense();
        expense.setUserId(request.getUserId());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory().toLowerCase());
        expense.setIsRegret(request.getIsRegret() != null ? request.getIsRegret() : false);
        return expenseRepository.save(expense);
    }

    public List<Expense> getExpensesByUserId(UUID userId) {
        return expenseRepository.findByUserId(userId);
    }

    public void deleteExpense(UUID id) {
        expenseRepository.deleteById(id);
    }

    @Autowired
    private com.pocketsense.repository.SavingRepository savingRepository;

    @Autowired
    private com.pocketsense.repository.GoalRepository goalRepository;

    public void resetUserData(UUID userId) {
        expenseRepository.deleteByUserId(userId);
        savingRepository.deleteByUserId(userId);
        goalRepository.deleteByUserId(userId);
    }
}
