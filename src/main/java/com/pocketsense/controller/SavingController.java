package com.pocketsense.controller;

import com.pocketsense.dto.SavingRequest;
import com.pocketsense.model.Saving;
import com.pocketsense.service.GoalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class SavingController {

    private static final Logger logger = LoggerFactory.getLogger(SavingController.class);

    @Autowired
    private GoalService goalService;

    @PostMapping("/addSaving")
    public ResponseEntity<?> addSaving(@RequestBody SavingRequest request) {
        try {
            if (request == null || request.getUserId() == null || request.getAmount() == null) {
                return ResponseEntity.badRequest().body("Invalid saving data");
            }
            Saving saving = goalService.saveSaving(request);
            return ResponseEntity.ok(saving);
        } catch (Exception e) {
            logger.error("Error adding saving: ", e);
            return ResponseEntity.internalServerError().body("Saving sync failed");
        }
    }

    @GetMapping("/savings/{userId}")
    public ResponseEntity<?> getSavings(@PathVariable UUID userId) {
        try {
            if (userId == null) return ResponseEntity.badRequest().body("User ID required");
            return ResponseEntity.ok(goalService.getSavingsByUserId(userId));
        } catch (Exception e) {
            logger.error("Error fetching savings for user {}: ", userId, e);
            return ResponseEntity.internalServerError().body("Error fetching savings");
        }
    }
}
