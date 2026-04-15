package com.pocketsense.controller;

import com.pocketsense.service.InsightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class RegretController {

    private static final Logger logger = LoggerFactory.getLogger(RegretController.class);

    @Autowired
    private InsightService insightService;

    @GetMapping("/regret/{userId}")
    public ResponseEntity<?> getRegret(@PathVariable UUID userId) {
        try {
            if (userId == null) return ResponseEntity.badRequest().body("User ID is required");
            return ResponseEntity.ok(insightService.getRegret(userId));
        } catch (Exception e) {
            logger.error("Error fetching regret data for user {}: ", userId, e);
            return ResponseEntity.internalServerError().body("Error processing regret metrics");
        }
    }
}
