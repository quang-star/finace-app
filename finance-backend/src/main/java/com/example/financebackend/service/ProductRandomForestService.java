package com.example.financebackend.service;

import com.example.financebackend.ml.ProductRandomForestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ProductRandomForestService {

    private static final Logger logger = LoggerFactory.getLogger(ProductRandomForestService.class);
    private static final double LOW_CONFIDENCE_THRESHOLD = 0.5;

    private static final String[] CLASS_NAMES = {
            "bottled_water", "bread", "clothes", "coffee_cup", "cosmetic",
            "electronic_item", "fastfood", "helmet", "medicine", "milk_tea",
            "motorbike", "noodle", "rice_meal", "shoes", "snack",
            "soft_drink", "taxi_car", "toy_game"
    };

    private static final Map<String, Integer> CLASS_INDEX = buildClassIndex();
    private static final Map<Integer, String> CLASS_ID_TO_NAME = buildClassIdToName();

    public ProductPrediction classify(List<DetectionInput> detections) {
        if (detections == null || detections.isEmpty()) {
            logger.info("Product RF skipped because YOLO detections are empty.");
            return null;
        }

        double[] features = extractFeatures(detections);
        double[] scores = ProductRandomForestModel.score(features);
        if (scores == null || scores.length == 0) {
            return null;
        }

        int bestIndex = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[bestIndex]) {
                bestIndex = i;
            }
        }

        if (bestIndex >= ProductRandomForestModel.CATEGORY_IDS.length) {
            return null;
        }

        int categoryId = ProductRandomForestModel.CATEGORY_IDS[bestIndex];
        String keyword = mapCategoryIdToKeyword(categoryId);
        String bestObject = findBestObjectName(detections);
        logger.info("YOLO object '{}' classified by RF as keyword='{}', categoryId={}, confidence={}",
                bestObject,
                keyword,
                categoryId,
                scores[bestIndex]);
        logger.info("Product RF result detections={}, features={}, scores={}, categoryId={}, keyword={}, confidence={}",
                detections.size(),
                Arrays.toString(features),
                Arrays.toString(scores),
                categoryId,
                keyword,
                scores[bestIndex]);
        return new ProductPrediction(categoryId, keyword, scores[bestIndex]);
    }

    private String findBestObjectName(List<DetectionInput> detections) {
        DetectionInput bestDetection = null;
        for (DetectionInput detection : detections) {
            if (detection == null) {
                continue;
            }
            if (bestDetection == null) {
                bestDetection = detection;
                continue;
            }

            double currentConfidence = detection.getConfidence() != null ? detection.getConfidence() : 0.0;
            double bestConfidence = bestDetection.getConfidence() != null ? bestDetection.getConfidence() : 0.0;
            if (currentConfidence > bestConfidence) {
                bestDetection = detection;
            }
        }

        if (bestDetection == null) {
            return "unknown";
        }

        String className = normalizeClassName(bestDetection);
        return className.isBlank() ? "unknown" : className;
    }

    public int getCompiledTreeCount() {
        return ProductRandomForestModel.CATEGORY_IDS.length;
    }

    private double[] extractFeatures(List<DetectionInput> detections) {
        double[] features = new double[ProductRandomForestModel.FEATURE_NAMES.length];
        double confidenceSum = 0.0;
        int total = 0;
        int lowConfidenceCount = 0;

        for (DetectionInput detection : detections) {
            if (detection == null) {
                continue;
            }

            String className = normalizeClassName(detection);
            double confidence = detection.getConfidence() != null ? detection.getConfidence() : 0.0;
            total++;
            confidenceSum += confidence;
            features[24] = Math.max(features[24], confidence);
            if (confidence < LOW_CONFIDENCE_THRESHOLD) {
                lowConfidenceCount++;
            }

            Integer classIndex = CLASS_INDEX.get(className);
            if (classIndex != null) {
                features[classIndex] = 1.0;
                incrementGroupCount(features, className);
            }
        }

        features[23] = total;
        features[25] = total > 0 ? confidenceSum / total : 0.0;
        features[26] = lowConfidenceCount;
        return features;
    }

    private String normalizeClassName(DetectionInput detection) {
        if (detection.getClassName() != null && !detection.getClassName().isBlank()) {
            return detection.getClassName().trim().toLowerCase(Locale.ROOT);
        }
        if (detection.getClassId() != null) {
            return CLASS_ID_TO_NAME.getOrDefault(detection.getClassId(), "");
        }
        return "";
    }

    private void incrementGroupCount(double[] features, String className) {
        switch (className) {
            case "bottled_water":
            case "bread":
            case "coffee_cup":
            case "fastfood":
            case "milk_tea":
            case "noodle":
            case "rice_meal":
            case "snack":
            case "soft_drink":
                features[18]++;
                break;
            case "motorbike":
            case "taxi_car":
                features[19]++;
                break;
            case "clothes":
            case "cosmetic":
            case "electronic_item":
            case "helmet":
            case "shoes":
                features[20]++;
                break;
            case "toy_game":
                features[21]++;
                break;
            case "medicine":
                features[22]++;
                break;
            default:
                break;
        }
    }

    private String mapCategoryIdToKeyword(int categoryId) {
        switch (categoryId) {
            case 1:
                return "food";
            case 2:
                return "transport";
            case 3:
                return "shopping";
            case 5:
                return "entertainment";
            case 6:
                return "health";
            default:
                return "other";
        }
    }

    private static Map<String, Integer> buildClassIndex() {
        Map<String, Integer> result = new HashMap<>();
        for (int i = 0; i < CLASS_NAMES.length; i++) {
            result.put(CLASS_NAMES[i], i);
        }
        return result;
    }

    private static Map<Integer, String> buildClassIdToName() {
        Map<Integer, String> result = new HashMap<>();
        for (int i = 0; i < CLASS_NAMES.length; i++) {
            result.put(i, CLASS_NAMES[i]);
        }
        return result;
    }

    public static class DetectionInput {
        private Integer classId;
        private String className;
        private Double confidence;
        private List<Double> bbox;

        public Integer getClassId() {
            return classId;
        }

        public void setClassId(Integer classId) {
            this.classId = classId;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public Double getConfidence() {
            return confidence;
        }

        public void setConfidence(Double confidence) {
            this.confidence = confidence;
        }

        public List<Double> getBbox() {
            return bbox;
        }

        public void setBbox(List<Double> bbox) {
            this.bbox = bbox;
        }
    }

    public static class ProductPrediction {
        private final int categoryId;
        private final String keyword;
        private final double confidenceScore;

        public ProductPrediction(int categoryId, String keyword, double confidenceScore) {
            this.categoryId = categoryId;
            this.keyword = keyword;
            this.confidenceScore = confidenceScore;
        }

        public int getCategoryId() {
            return categoryId;
        }

        public String getKeyword() {
            return keyword;
        }

        public double getConfidenceScore() {
            return confidenceScore;
        }
    }
}
