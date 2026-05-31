package com.example.financebackend.controller;

import com.example.financebackend.dto.ApiResponse;
import com.example.financebackend.model.AiScanLog;
import com.example.financebackend.model.Category;
import com.example.financebackend.repository.CategoryRepository;
import com.example.financebackend.service.AiScanService;
import com.example.financebackend.service.ExpenseClassifierService;
import com.example.financebackend.service.ocr.ReceiptData;
import com.example.financebackend.service.ocr.ReceiptFeatureExtractor;
import com.example.financebackend.service.ocr.ReceiptFeatures;
import com.example.financebackend.service.ocr.ReceiptParser;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/ai-scan")
public class AiScanController {

    private static final Logger logger = LoggerFactory.getLogger(AiScanController.class);

    private final AiScanService aiScanService;
    private final ExpenseClassifierService expenseClassifierService;
    private final CategoryRepository categoryRepository;
    private final ReceiptParser receiptParser;
    private final ReceiptFeatureExtractor receiptFeatureExtractor;

    public AiScanController(AiScanService aiScanService,
                              ExpenseClassifierService expenseClassifierService,
                              CategoryRepository categoryRepository,
                              ReceiptParser receiptParser,
                              ReceiptFeatureExtractor receiptFeatureExtractor) {
        this.aiScanService = aiScanService;
        this.expenseClassifierService = expenseClassifierService;
        this.categoryRepository = categoryRepository;
        this.receiptParser = receiptParser;
        this.receiptFeatureExtractor = receiptFeatureExtractor;
    }

    @PostMapping("/classify")
    public ResponseEntity<ApiResponse<AiScanResult>> classifyBill(@RequestBody OcrRequest request) {
        try {
            String rawText = request.getRawOcrText();
            if (rawText == null || rawText.isBlank()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("OCR text is empty"));
            }

            ReceiptData receipt = receiptParser.parse(rawText);
            ReceiptFeatures features = receiptFeatureExtractor.extract(receipt);
            BigDecimal amount = receipt.getAmount();
            LocalDate date = receipt.getDate();
            String merchant = receipt.getMerchant();
            logger.info("OCR classify request userId={}, rawLength={}", request.getUserId(), rawText.length());
            logger.info("OCR raw text:\n{}", rawText);
            logger.info("OCR normalized text:\n{}", receipt.getNormalizedText());
            logger.info("OCR parsed fields merchant='{}', amount={}, date={}, hour={}",
                    merchant, amount, date, receipt.getHour());
            logger.info("OCR feature vector={}", features.getVector());

            String categoryKeyword = expenseClassifierService.classify(features);
            if ("other".equals(categoryKeyword)) {
                categoryKeyword = expenseClassifierService.classify(rawText);
            }
            
            // 5. Look up Category ID in DB matching this keyword
            Category matchedCategory = findCategoryInDb(request.getUserId(), categoryKeyword);
            Integer categoryId = matchedCategory != null ? matchedCategory.getCategoryId() : null;
            String categoryName = matchedCategory != null ? matchedCategory.getCategoryName() : "Khác";
            logger.info("OCR category result keyword='{}', categoryId={}, categoryName='{}'",
                    categoryKeyword, categoryId, categoryName);

            // 6. Save log
            AiScanLog log = aiScanService.saveLog(
                    request.getUserId(),
                    rawText,
                    merchant,
                    amount,
                    date,
                    categoryId,
                    estimateConfidence(features, categoryKeyword)
            );

            AiScanResult result = new AiScanResult();
            result.setAiScanLogId(log.getAiScanLogId());
            result.setDetectedMerchant(merchant);
            result.setDetectedAmount(amount);
            result.setDetectedDate(date);
            result.setSuggestedCategoryId(categoryId);
            result.setSuggestedCategoryName(categoryName);
            result.setConfidenceScore(estimateConfidence(features, categoryKeyword));
            result.setFeatureVector(features.getVector());

            return ResponseEntity.ok(ApiResponse.success("Bill scanned successfully", result));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/feedback")
    public ResponseEntity<ApiResponse<String>> saveFeedback(@RequestBody ScanFeedbackRequest request) {
        try {
            if (request.getAiScanLogId() == null || request.getAiScanLogId() <= 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("AI scan log id is required"));
            }
            aiScanService.saveFeedback(
                    request.getAiScanLogId(),
                    request.getTransactionId(),
                    request.getActualCategoryId()
            );
            return ResponseEntity.ok(ApiResponse.success("Feedback saved successfully", "Thank you for your feedback"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private Category findCategoryInDb(Integer userId, String keyword) {
        List<Category> categories = categoryRepository.findByUserUserIdOrIsDefaultTrue(userId);
        
        // Map keyword to Vietnamese standard names
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

    private BigDecimal estimateConfidence(ReceiptFeatures features, String categoryKeyword) {
        if (features == null || categoryKeyword == null || "other".equalsIgnoreCase(categoryKeyword)) {
            return BigDecimal.valueOf(0.55);
        }

        int signalCount = features.getFoodKeywordCount()
                + features.getDrinkKeywordCount()
                + features.getTransportKeywordCount()
                + features.getShoppingKeywordCount()
                + features.getBillKeywordCount()
                + features.getMerchantSupermarket()
                + features.getMerchantCafe()
                + features.getMerchantTransport()
                + features.getMerchantConvenienceStore();

        if (signalCount >= 3) {
            return BigDecimal.valueOf(0.88);
        }
        if (signalCount == 2) {
            return BigDecimal.valueOf(0.78);
        }
        return BigDecimal.valueOf(0.65);
    }

    private BigDecimal extractAmount(String text) {
        // Look for common total patterns like "tổng cộng", "tong tien", "total", "thanh toan" followed by numbers
        Pattern[] patterns = {
                Pattern.compile("(?i)(tổng cộng|tong tien|tổng tiền|total|thành tiền|thanh toan|cần trả):?\\s*([\\d.,]+)"),
                Pattern.compile("(?i)vnd\\s*([\\d.,]+)"),
                Pattern.compile("(?i)([\\d.,]+)\\s*(vnd|đ|d)")
        };

        for (Pattern p : patterns) {
            Matcher m = p.matcher(text);
            if (m.find()) {
                int groupIdx = 1;
                if (m.groupCount() >= 2) {
                    String g2 = m.group(2);
                    if (g2 != null && g2.matches("[\\d.,\\s]+")) {
                        groupIdx = 2;
                    }
                }
                String clean = m.group(groupIdx)
                        .replace(".", "")
                        .replace(",", "")
                        .trim();
                try {
                    return new BigDecimal(clean);
                } catch (Exception ignored) {}
            }
        }

        // Fallback: look for the largest number in the text (often the total amount)
        Pattern numberPattern = Pattern.compile("\\b(\\d{1,3}([,.]\\d{3})+|\\d{4,9})\\b");
        Matcher matcher = numberPattern.matcher(text);
        BigDecimal maxVal = BigDecimal.ZERO;
        while (matcher.find()) {
            String valStr = matcher.group(0).replace(".", "").replace(",", "");
            try {
                BigDecimal val = new BigDecimal(valStr);
                if (val.compareTo(maxVal) > 0 && val.compareTo(BigDecimal.valueOf(50000000)) < 0) { // Limit to 50M for realistic bills
                    maxVal = val;
                }
            } catch (Exception ignored) {}
        }

        return maxVal.compareTo(BigDecimal.ZERO) > 0 ? maxVal : BigDecimal.valueOf(0);
    }

    private LocalDate extractDate(String text) {
        // Match dd/MM/yyyy, dd-MM-yyyy, yyyy-MM-dd
        Pattern[] patterns = {
                Pattern.compile("\\b(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})\\b"),
                Pattern.compile("\\b(\\d{4})[-/](\\d{1,2})[-/](\\d{1,2})\\b")
        };

        for (Pattern p : patterns) {
            Matcher m = p.matcher(text);
            if (m.find()) {
                try {
                    if (p.toString().startsWith("\\b(\\d{1,2})")) {
                        int day = Integer.parseInt(m.group(1));
                        int month = Integer.parseInt(m.group(2));
                        int year = Integer.parseInt(m.group(3));
                        return LocalDate.of(year, month, day);
                    } else {
                        int year = Integer.parseInt(m.group(1));
                        int month = Integer.parseInt(m.group(2));
                        int day = Integer.parseInt(m.group(3));
                        return LocalDate.of(year, month, day);
                    }
                } catch (Exception ignored) {}
            }
        }
        return LocalDate.now(); // default to today
    }

    private String extractMerchant(String text) {
        String[] lines = text.split("\\n");
        if (lines.length > 0) {
            // Usually, the first 1-3 lines contain the merchant name
            for (int i = 0; i < Math.min(3, lines.length); i++) {
                String line = lines[i].trim();
                if (line.length() > 3 && !line.matches("\\d+") && !line.toLowerCase().contains("dia chi") && !line.toLowerCase().contains("địa chỉ")) {
                    return line;
                }
            }
        }
        return "Cửa hàng bán lẻ";
    }

    @Data
    public static class OcrRequest {
        private Integer userId;
        private String rawOcrText;
    }

    @Data
    public static class ScanFeedbackRequest {
        private Integer aiScanLogId;
        private Integer transactionId;
        private Integer actualCategoryId;
    }

    @Data
    public static class AiScanResult {
        private Integer aiScanLogId;
        private String detectedMerchant;
        private BigDecimal detectedAmount;
        private LocalDate detectedDate;
        private Integer suggestedCategoryId;
        private String suggestedCategoryName;
        private BigDecimal confidenceScore;
        private List<Double> featureVector;
    }
}
