package com.pocketsense.controller;

import com.pocketsense.dto.AlertResponse;
import com.pocketsense.service.InsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AlertController {

    @Autowired
    private InsightService insightService;

    @GetMapping("/alerts/{userId}")
    public ResponseEntity<List<AlertResponse>> getAlerts(@PathVariable UUID userId) {
        List<AlertResponse> alerts = insightService.getAlerts(userId);
        return ResponseEntity.ok(alerts);
    }
}
