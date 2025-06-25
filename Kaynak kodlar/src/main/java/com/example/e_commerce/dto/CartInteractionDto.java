package com.example.e_commerce.dto;

public class CartInteractionDto {
    private Long productId;
    private int quantity;
    private Double value;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
}
