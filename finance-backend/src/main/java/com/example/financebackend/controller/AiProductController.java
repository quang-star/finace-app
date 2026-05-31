package com.example.financebackend.controller;

import com.example.financebackend.dto.ApiResponse;
import com.example.financebackend.model.AiProductLog;
import com.example.financebackend.model.Category;
import com.example.financebackend.repository.CategoryRepository;
import com.example.financebackend.service.AiProductService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/ai-product")
public class AiProductController {

    private final AiProductService aiProductService;
    private final CategoryRepository categoryRepository;

    public AiProductController(AiProductService aiProductService,
                               CategoryRepository categoryRepository) {
        this.aiProductService = aiProductService;
        this.categoryRepository = categoryRepository;
    }

    @PostMapping("/classify")
    public ResponseEntity<ApiResponse<AiProductClassificationResult>> classifyProduct(@RequestBody ProductClassifyRequest request) {
        try {
            String keyword = aiProductService.classifyProduct(request.getProductName());
            Category matchedCategory = findCategoryInDb(request.getUserId(), keyword);
            
            Integer categoryId = matchedCategory != null ? matchedCategory.getCategoryId() : null;
            String categoryName = matchedCategory != null ? matchedCategory.getCategoryName() : "Khác";

            AiProductClassificationResult result = new AiProductClassificationResult();
            result.setSuggestedCategoryId(categoryId);
            result.setSuggestedCategoryName(categoryName);
            result.setConfidenceScore(BigDecimal.valueOf(0.90)); // default confidence for YOLO + keyword

            return ResponseEntity.ok(ApiResponse.success("Product classified successfully", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/log")
    public ResponseEntity<ApiResponse<AiProductLog>> saveLog(@RequestBody SaveProductLogRequest request) {
        try {
            AiProductLog log = aiProductService.saveProductLog(
                    request.getUserId(),
                    request.getDetectedProduct(),
                    request.getConfidenceScore(),
                    request.getUserEnteredPrice(),
                    request.getSuggestedCategoryId()
            );
            return ResponseEntity.ok(ApiResponse.success("Product log saved successfully", log));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private Category findCategoryInDb(Integer userId, String keyword) {
        List<Category> categories = categoryRepository.findByUserUserIdOrIsDefaultTrue(userId);
        
        String vietnameseName;
        switch (keyword.toLowerCase()) {
            case "food":
                vietnameseName = "Ăn uống";
                break;
            case "transport":
                vietnameseName = "Di chuyển";
                break;
            case "shopping":
                vietnameseName = "Mua sắm";
                break;
            case "bills":
                vietnameseName = "Hóa đơn";
                break;
            case "entertainment":
                vietnameseName = "Giải trí";
                break;
            case "health":
                vietnameseName = "Sức khỏe";
                break;
            case "education":
                vietnameseName = "Giáo dục";
                break;
            default:
                vietnameseName = "Khác";
        }

        // Try exact match
        for (Category cat : categories) {
            if (cat.getCategoryName().equalsIgnoreCase(vietnameseName) || 
                cat.getCategoryName().toLowerCase().contains(keyword.toLowerCase())) {
                return cat;
            }
        }

        // Try to find a default expense category
        for (Category cat : categories) {
            if ("expense".equalsIgnoreCase(cat.getCategoryType())) {
                return cat;
            }
        }

        return null;
    }

    @Data
    public static class ProductClassifyRequest {
        private Integer userId;
        private String productName;
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
