package com.pocketsense.controller;

import com.pocketsense.dto.AlertResponse;
import com.pocketsense.service.InsightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AlertController {

    private static final Logger logger = LoggerFactory.getLogger(AlertController.class);

    @Autowired
    private InsightService insightService;

    @GetMapping("/alerts/{userId}")
    public ResponseEntity<?> getAlerts(@PathVariable UUID userId) {
        try {
            if (userId == null) return ResponseEntity.badRequest().body("User ID required");
            List<AlertResponse> alerts = insightService.getAlerts(userId);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            logger.error("Error fetching alerts for user {}: ", userId, e);
            return ResponseEntity.internalServerError().body("Alert system temporarily unavailable");
        }
    }
}
