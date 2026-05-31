package com.example.personalfinance.models;

public class AiProductLog {
    private Integer aiProductLogId;
    private Integer userId;
    private Integer transactionId;
    private String detectedProduct;
    private double confidenceScore;
    private double userEnteredPrice;
    private Integer suggestedCategoryId;
    private String createdAt;

    public AiProductLog() {}

    public AiProductLog(Integer aiProductLogId, Integer userId, Integer transactionId, String detectedProduct, double confidenceScore, double userEnteredPrice, Integer suggestedCategoryId, String createdAt) {
        this.aiProductLogId = aiProductLogId;
        this.userId = userId;
        this.transactionId = transactionId;
        this.detectedProduct = detectedProduct;
        this.confidenceScore = confidenceScore;
        this.userEnteredPrice = userEnteredPrice;
        this.suggestedCategoryId = suggestedCategoryId;
        this.createdAt = createdAt;
    }

    public Integer getAiProductLogId() {
        return aiProductLogId;
    }

    public void setAiProductLogId(Integer aiProductLogId) {
        this.aiProductLogId = aiProductLogId;
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

    public String getDetectedProduct() {
        return detectedProduct;
    }

    public void setDetectedProduct(String detectedProduct) {
        this.detectedProduct = detectedProduct;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public double getUserEnteredPrice() {
        return userEnteredPrice;
    }

    public void setUserEnteredPrice(double userEnteredPrice) {
        this.userEnteredPrice = userEnteredPrice;
    }

    public Integer getSuggestedCategoryId() {
        return suggestedCategoryId;
    }

    public void setSuggestedCategoryId(Integer suggestedCategoryId) {
        this.suggestedCategoryId = suggestedCategoryId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
