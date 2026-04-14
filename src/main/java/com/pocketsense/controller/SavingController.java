package com.pocketsense.controller;

import com.pocketsense.dto.SavingRequest;
import com.pocketsense.model.Saving;
import com.pocketsense.service.SavingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class SavingController {

    @Autowired
    private SavingService savingService;

    @PostMapping("/addSaving")
    public ResponseEntity<Saving> addSaving(@RequestBody SavingRequest request) {
        Saving saving = savingService.addSaving(request);
        return ResponseEntity.ok(saving);
    }

    @GetMapping("/savings/{userId}")
    public ResponseEntity<List<Saving>> getSavings(@PathVariable UUID userId) {
        List<Saving> savings = savingService.getSavingsByUserId(userId);
        return ResponseEntity.ok(savings);
    }
}
