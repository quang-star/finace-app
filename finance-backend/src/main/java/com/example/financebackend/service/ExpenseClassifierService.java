package com.example.financebackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.example.financebackend.service.ocr.ReceiptFeatures;

/**
 * Expense classifier service using rule-based classification.
 * Can be extended with Smile ML library for more sophisticated classification.
 */
@Service
public class ExpenseClassifierService {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseClassifierService.class);

    private final Map<String, String> keywordCategoryMap;

    public ExpenseClassifierService() {
        keywordCategoryMap = new HashMap<>();
        // Food & Beverage
        keywordCategoryMap.put("com", "food");
        keywordCategoryMap.put("pho", "food");
        keywordCategoryMap.put("bun", "food");
        keywordCategoryMap.put("cafe", "food");
        keywordCategoryMap.put("coffee", "food");
        keywordCategoryMap.put("tra", "food");
        keywordCategoryMap.put("sua", "food");
        keywordCategoryMap.put("banh", "food");
        keywordCategoryMap.put("rice", "food");
        keywordCategoryMap.put("chicken", "food");
        keywordCategoryMap.put("ga", "food");
        keywordCategoryMap.put("bo", "food");

        // Transport
        keywordCategoryMap.put("grab", "transport");
        keywordCategoryMap.put("taxi", "transport");
        keywordCategoryMap.put("xang", "transport");
        keywordCategoryMap.put("petrol", "transport");
        keywordCategoryMap.put("bus", "transport");
        keywordCategoryMap.put("parking", "transport");

        // Shopping
        keywordCategoryMap.put("ao", "shopping");
        keywordCategoryMap.put("quan", "shopping");
        keywordCategoryMap.put("giay", "shopping");
        keywordCategoryMap.put("dep", "shopping");
        keywordCategoryMap.put("clothes", "shopping");
        keywordCategoryMap.put("shoes", "shopping");

        // Bills
        keywordCategoryMap.put("dien", "bills");
        keywordCategoryMap.put("nuoc", "bills");
        keywordCategoryMap.put("internet", "bills");
        keywordCategoryMap.put("phone", "bills");
        keywordCategoryMap.put("electric", "bills");
        keywordCategoryMap.put("water", "bills");

        // Entertainment
        keywordCategoryMap.put("phim", "entertainment");
        keywordCategoryMap.put("game", "entertainment");
        keywordCategoryMap.put("movie", "entertainment");
        keywordCategoryMap.put("karaoke", "entertainment");

        // Health
        keywordCategoryMap.put("thuoc", "health");
        keywordCategoryMap.put("medicine", "health");
        keywordCategoryMap.put("doctor", "health");
        keywordCategoryMap.put("hospital", "health");
        keywordCategoryMap.put("benh vien", "health");

        // Education
        keywordCategoryMap.put("sach", "education");
        keywordCategoryMap.put("book", "education");
        keywordCategoryMap.put("hoc", "education");
        keywordCategoryMap.put("course", "education");
    }

    public String classify(String productName) {
        if (productName == null || productName.isBlank()) {
            return "other";
        }

        String lowerName = productName.toLowerCase().trim();

        for (Map.Entry<String, String> entry : keywordCategoryMap.entrySet()) {
            if (lowerName.contains(entry.getKey())) {
                logger.info("Classified '{}' as '{}' (matched keyword: '{}')", productName, entry.getValue(), entry.getKey());
                return entry.getValue();
            }
        }

        logger.info("Could not classify '{}', defaulting to 'other'", productName);
        return "other";
    }

    public String classify(ReceiptFeatures features) {
        if (features == null) {
            return "other";
        }

        int foodScore = features.getFoodKeywordCount() + features.getDrinkKeywordCount();
        int transportScore = features.getTransportKeywordCount() + features.getMerchantTransport();
        int shoppingScore = features.getShoppingKeywordCount()
                + features.getMerchantSupermarket()
                + features.getMerchantConvenienceStore();
        int billScore = features.getBillKeywordCount();

        String bestCategory = "other";
        int bestScore = 0;

        if (foodScore > bestScore) {
            bestScore = foodScore;
            bestCategory = "food";
        }
        if (transportScore > bestScore) {
            bestScore = transportScore;
            bestCategory = "transport";
        }
        if (shoppingScore > bestScore) {
            bestScore = shoppingScore;
            bestCategory = "shopping";
        }
        if (billScore > bestScore) {
            bestCategory = "bills";
        }

        logger.info("Classified receipt features as '{}'", bestCategory);
        return bestCategory;
    }

    public void retrain() {
        // Placeholder for ML model retraining using Smile library
        logger.info("Model retraining triggered. This is a placeholder implementation.");
        logger.info("In production, this would load training data from ai_product_logs and retrain the classifier.");
    }
}
