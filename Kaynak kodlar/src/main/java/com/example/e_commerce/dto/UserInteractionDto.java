package com.example.e_commerce.dto;

public class UserInteractionDto {
    private Long categoryId;
    private Long productId;
    private Double value;

    public Long getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getProductId() {
        return productId;
    }
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Double getValue() {
        return value;
    }
    public void setValue(Double value) {
        this.value = value;
    }
}
