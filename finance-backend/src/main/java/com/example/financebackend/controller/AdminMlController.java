package com.example.financebackend.controller;

import com.example.financebackend.dto.ApiResponse;
import com.example.financebackend.service.ExpenseClassifierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ml")
public class AdminMlController {

    private final ExpenseClassifierService classifierService;

    public AdminMlController(ExpenseClassifierService classifierService) {
        this.classifierService = classifierService;
    }

    @PostMapping("/retrain")
    public ResponseEntity<ApiResponse<String>> retrain() {
        try {
            classifierService.retrain();
            return ResponseEntity.ok(ApiResponse.success("Model retrained successfully", "Retraining completed."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
