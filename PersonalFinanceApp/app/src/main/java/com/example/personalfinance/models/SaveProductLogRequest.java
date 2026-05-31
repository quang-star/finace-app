package com.example.personalfinance.models;

public class SaveProductLogRequest {
    private Integer userId;
    private String detectedProduct;
    private double confidenceScore;
    private double userEnteredPrice;
    private Integer suggestedCategoryId;

    public SaveProductLogRequest() {}

    public SaveProductLogRequest(Integer userId, String detectedProduct, double confidenceScore, double userEnteredPrice, Integer suggestedCategoryId) {
        this.userId = userId;
        this.detectedProduct = detectedProduct;
        this.confidenceScore = confidenceScore;
        this.userEnteredPrice = userEnteredPrice;
        this.suggestedCategoryId = suggestedCategoryId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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
}
