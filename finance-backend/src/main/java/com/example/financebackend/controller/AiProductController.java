package com.example.financebackend.controller;

import com.example.financebackend.dto.ApiResponse;
import com.example.financebackend.model.AiProductLog;
import com.example.financebackend.model.Category;
import com.example.financebackend.service.AiProductService;
import com.example.financebackend.service.CategoryService;
import com.example.financebackend.service.ProductRandomForestService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/ai-product")
public class AiProductController {

    private static final Logger logger = LoggerFactory.getLogger(AiProductController.class);

    private final AiProductService aiProductService;
    private final CategoryService categoryService;

    public AiProductController(AiProductService aiProductService,
                               CategoryService categoryService) {
        this.aiProductService = aiProductService;
        this.categoryService = categoryService;
    }

    @PostMapping("/classify")
    public ResponseEntity<ApiResponse<AiProductClassificationResult>> classifyProduct(@RequestBody ProductClassifyRequest request) {
        logProductClassifyRequest(request);

        ProductRandomForestService.ProductPrediction prediction =
                aiProductService.classifyProductDetections(request.getDetections());
        String keyword = prediction != null ? prediction.getKeyword() : aiProductService.classifyProduct(request.getProductName());
        Category matchedCategory = categoryService.findCategoryByKeyword(request.getUserId(), keyword);

        Integer categoryId = matchedCategory != null ? matchedCategory.getCategoryId() : null;
        String categoryName = matchedCategory != null ? matchedCategory.getCategoryName() : "Khác";

        AiProductClassificationResult result = new AiProductClassificationResult();
        result.setSuggestedCategoryId(categoryId);
        result.setSuggestedCategoryName(categoryName);
        double confidenceScore = prediction != null ? prediction.getConfidenceScore() : 0.90;
        result.setConfidenceScore(BigDecimal.valueOf(confidenceScore));

        return ResponseEntity.ok(ApiResponse.success("Product classified successfully", result));
    }

    private void logProductClassifyRequest(ProductClassifyRequest request) {
        List<ProductRandomForestService.DetectionInput> detections = request.getDetections();
        logger.info("YOLO classify request userId={}, productName='{}', detections={}",
                request.getUserId(),
                request.getProductName(),
                detections != null ? detections.size() : 0);

        if (detections == null || detections.isEmpty()) {
            logger.info("YOLO detections empty.");
            return;
        }

        ProductRandomForestService.DetectionInput bestDetection = detections.stream()
                .filter(detection -> detection.getConfidence() != null)
                .max((left, right) -> Double.compare(left.getConfidence(), right.getConfidence()))
                .orElse(detections.get(0));

        logger.info("YOLO best object classId={}, className='{}', confidence={}, bbox={}",
                bestDetection.getClassId(),
                bestDetection.getClassName(),
                bestDetection.getConfidence(),
                bestDetection.getBbox());

        for (ProductRandomForestService.DetectionInput detection : detections) {
            logger.info("YOLO detection classId={}, className='{}', confidence={}, bbox={}",
                    detection.getClassId(),
                    detection.getClassName(),
                    detection.getConfidence(),
                    detection.getBbox());
        }
    }

    @PostMapping("/log")
    public ResponseEntity<ApiResponse<AiProductLog>> saveLog(@RequestBody SaveProductLogRequest request) {
        AiProductLog log = aiProductService.saveProductLog(
                request.getUserId(),
                request.getDetectedProduct(),
                request.getConfidenceScore(),
                request.getUserEnteredPrice(),
                request.getSuggestedCategoryId()
        );
        return ResponseEntity.ok(ApiResponse.success("Product log saved successfully", log));
    }

    @Data
    public static class ProductClassifyRequest {
        private Integer userId;
        private String productName;
        private String imageName;
        private List<ProductRandomForestService.DetectionInput> detections;
    }

    @Data
    public static class SaveProductLogRequest {
        private Integer userId;
        private String detectedProduct;
        private BigDecimal confidenceScore;
        private BigDecimal userEnteredPrice;
        private Integer suggestedCategoryId;
    }

    @Data
    public static class AiProductClassificationResult {
        private Integer suggestedCategoryId;
        private String suggestedCategoryName;
        private BigDecimal confidenceScore;
    }
}
