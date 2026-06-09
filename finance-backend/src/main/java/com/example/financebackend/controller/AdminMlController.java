package com.example.financebackend.controller;

import com.example.financebackend.dto.ApiResponse;
import com.example.financebackend.service.ExpenseClassifierService;
import com.example.financebackend.service.ProductRandomForestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ml")
public class AdminMlController {

    private final ExpenseClassifierService classifierService;
    private final ProductRandomForestService productRandomForestService;

    public AdminMlController(ExpenseClassifierService classifierService,
                             ProductRandomForestService productRandomForestService) {
        this.classifierService = classifierService;
        this.productRandomForestService = productRandomForestService;
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

    @PostMapping("/reload-product")
    public ResponseEntity<ApiResponse<String>> reloadProduct() {
        try {
            int classes = productRandomForestService.getCompiledTreeCount();
            return ResponseEntity.ok(ApiResponse.success(
                    "Product model is compiled into Java",
                    "Loaded " + classes + " output classes. Rebuild backend after retraining."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
