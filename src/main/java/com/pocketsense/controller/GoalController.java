package com.pocketsense.controller;

import com.pocketsense.dto.GoalRequest;
import com.pocketsense.dto.GoalResponse;
import com.pocketsense.model.Goal;
import com.pocketsense.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class GoalController {

    @Autowired
    private GoalService goalService;

    @PostMapping("/setGoal")
    public ResponseEntity<Goal> setGoal(@RequestBody GoalRequest request) {
        Goal goal = goalService.setGoal(request);
        return ResponseEntity.ok(goal);
    }

    @GetMapping("/goal/{userId}")
    public ResponseEntity<GoalResponse> getGoal(@PathVariable UUID userId) {
        try {
            GoalResponse response = goalService.getGoalResponse(userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
