package com.example.personalfinance.models;

public class AiProductClassificationResult {
    private Integer suggestedCategoryId;
    private String suggestedCategoryName;
    private double confidenceScore;
    public AiProductClassificationResult() {}

    public AiProductClassificationResult(Integer suggestedCategoryId, String suggestedCategoryName, double confidenceScore) {
        this.suggestedCategoryId = suggestedCategoryId;
        this.suggestedCategoryName = suggestedCategoryName;
        this.confidenceScore = confidenceScore;
    }

    public Integer getSuggestedCategoryId() {
        return suggestedCategoryId;
    }

    public void setSuggestedCategoryId(Integer suggestedCategoryId) {
        this.suggestedCategoryId = suggestedCategoryId;
    }

    public String getSuggestedCategoryName() {
        return suggestedCategoryName;
    }

    public void setSuggestedCategoryName(String suggestedCategoryName) {
        this.suggestedCategoryName = suggestedCategoryName;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
}
