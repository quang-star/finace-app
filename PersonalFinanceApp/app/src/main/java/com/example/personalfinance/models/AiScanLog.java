package com.example.personalfinance.models;

public class AiScanLog {
    private Integer aiScanLogId;
    private Integer userId;
    private Integer transactionId;
    private String rawOcrText;
    private String detectedMerchant;
    private double detectedAmount;
    private String detectedDate;
    private Integer suggestedCategoryId;
    private double confidenceScore;
    private String createdAt;

    public AiScanLog() {}

    public AiScanLog(Integer aiScanLogId, Integer userId, Integer transactionId, String rawOcrText, String detectedMerchant, double detectedAmount, String detectedDate, Integer suggestedCategoryId, double confidenceScore, String createdAt) {
        this.aiScanLogId = aiScanLogId;
        this.userId = userId;
        this.transactionId = transactionId;
        this.rawOcrText = rawOcrText;
        this.detectedMerchant = detectedMerchant;
        this.detectedAmount = detectedAmount;
        this.detectedDate = detectedDate;
        this.suggestedCategoryId = suggestedCategoryId;
        this.confidenceScore = confidenceScore;
        this.createdAt = createdAt;
    }

    public Integer getAiScanLogId() {
        return aiScanLogId;
    }

    public void setAiScanLogId(Integer aiScanLogId) {
        this.aiScanLogId = aiScanLogId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public String getRawOcrText() {
        return rawOcrText;
    }

    public void setRawOcrText(String rawOcrText) {
        this.rawOcrText = rawOcrText;
    }

    public String getDetectedMerchant() {
        return detectedMerchant;
    }

    public void setDetectedMerchant(String detectedMerchant) {
        this.detectedMerchant = detectedMerchant;
    }

    public double getDetectedAmount() {
        return detectedAmount;
    }

    public void setDetectedAmount(double detectedAmount) {
        this.detectedAmount = detectedAmount;
    }

    public String getDetectedDate() {
        return detectedDate;
    }

    public void setDetectedDate(String detectedDate) {
        this.detectedDate = detectedDate;
    }

    public Integer getSuggestedCategoryId() {
        return suggestedCategoryId;
    }

    public void setSuggestedCategoryId(Integer suggestedCategoryId) {
        this.suggestedCategoryId = suggestedCategoryId;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
