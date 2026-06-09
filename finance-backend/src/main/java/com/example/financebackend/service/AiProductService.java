package com.example.financebackend.service;

import com.example.financebackend.model.AiProductLog;
import com.example.financebackend.model.Category;
import com.example.financebackend.model.User;
import com.example.financebackend.repository.AiProductLogRepository;
import com.example.financebackend.repository.CategoryRepository;
import com.example.financebackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AiProductService {

    private final AiProductLogRepository aiProductLogRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseClassifierService expenseClassifierService;
    private final ProductRandomForestService productRandomForestService;

    public AiProductService(AiProductLogRepository aiProductLogRepository,
                            UserRepository userRepository,
                            CategoryRepository categoryRepository,
                            ExpenseClassifierService expenseClassifierService,
                            ProductRandomForestService productRandomForestService) {
        this.aiProductLogRepository = aiProductLogRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.expenseClassifierService = expenseClassifierService;
        this.productRandomForestService = productRandomForestService;
    }

    @Transactional
    public AiProductLog saveProductLog(Integer userId, String detectedProduct,
                                        BigDecimal confidenceScore, BigDecimal userEnteredPrice,
                                        Integer suggestedCategoryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Category suggestedCategory = null;
        if (suggestedCategoryId != null) {
            suggestedCategory = categoryRepository.findById(suggestedCategoryId)
                    .orElse(null);
        }

        AiProductLog log = AiProductLog.builder()
                .user(user)
                .detectedProduct(detectedProduct)
                .confidenceScore(confidenceScore)
                .userEnteredPrice(userEnteredPrice)
                .suggestedCategory(suggestedCategory)
                .build();

        return aiProductLogRepository.save(log);
    }

    public String classifyProduct(String productName) {
        return expenseClassifierService.classify(productName);
    }

    public ProductRandomForestService.ProductPrediction classifyProductDetections(
            List<ProductRandomForestService.DetectionInput> detections) {
        return productRandomForestService.classify(detections);
    }

    public List<AiProductLog> getLogsByUser(Integer userId) {
        return aiProductLogRepository.findByUserUserId(userId);
    }
}
