package com.example.personalfinance.models;

import java.util.List;

public class ProductClassifyRequest {
    private Integer userId;
    private String productName;
    private String imageName;
    private List<Detection> detections;

    public ProductClassifyRequest() {}

    public ProductClassifyRequest(Integer userId, String productName) {
        this.userId = userId;
        this.productName = productName;
    }

    public ProductClassifyRequest(Integer userId, String productName, List<Detection> detections) {
        this.userId = userId;
        this.productName = productName;
        this.detections = detections;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public List<Detection> getDetections() {
        return detections;
    }

    public void setDetections(List<Detection> detections) {
        this.detections = detections;
    }

    public static class Detection {
        private Integer classId;
        private String className;
        private Double confidence;
        private List<Double> bbox;

        public Detection() {}

        public Detection(Integer classId, String className, Double confidence, List<Double> bbox) {
            this.classId = classId;
            this.className = className;
            this.confidence = confidence;
            this.bbox = bbox;
        }

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
}
