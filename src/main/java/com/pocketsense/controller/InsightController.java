package com.pocketsense.controller;

import com.pocketsense.dto.InsightResponse;
import com.pocketsense.service.InsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class InsightController {

    @Autowired
    private InsightService insightService;

    @GetMapping("/insights/{userId}")
    public ResponseEntity<InsightResponse> getInsights(@PathVariable UUID userId) {
        InsightResponse response = insightService.getInsights(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health/{userId}")
    public ResponseEntity<com.pocketsense.dto.HealthResponse> getHealthScore(@PathVariable UUID userId) {
        return ResponseEntity.ok(insightService.getHealthScore(userId));
    }

    @GetMapping("/prediction/{userId}")
    public ResponseEntity<com.pocketsense.dto.PredictionResponse> getPrediction(@PathVariable UUID userId) {
        return ResponseEntity.ok(insightService.getPrediction(userId));
    }

    @GetMapping("/regret/{userId}")
    public ResponseEntity<com.pocketsense.dto.RegretResponse> getRegret(@PathVariable UUID userId) {
        return ResponseEntity.ok(insightService.getRegret(userId));
    }
}
