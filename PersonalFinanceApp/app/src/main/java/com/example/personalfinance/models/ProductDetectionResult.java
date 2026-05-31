package com.example.personalfinance.models;

import android.graphics.RectF;

public class ProductDetectionResult {
    private String label;
    private float confidence;
    private RectF boundingBox;

    public ProductDetectionResult() {}

    public ProductDetectionResult(String label, float confidence, RectF boundingBox) {
        this.label = label;
        this.confidence = confidence;
        this.boundingBox = boundingBox;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public RectF getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(RectF boundingBox) {
        this.boundingBox = boundingBox;
    }
}
