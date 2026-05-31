package com.example.personalfinance.models;

public class ProductClassifyRequest {
    private Integer userId;
    private String productName;

    public ProductClassifyRequest() {}

    public ProductClassifyRequest(Integer userId, String productName) {
        this.userId = userId;
        this.productName = productName;
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
}
