package com.example.orderservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private String id;
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Items are required")
    private List<OrderItem> items;
    
    @Positive(message = "Total amount must be positive")
    private Double totalAmount;
    
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;

    // Constructors
    public Order() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    public Order(String userId, List<OrderItem> items, Double totalAmount) {
        this();
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Inner class for Order Items
    public static class OrderItem {
        @NotBlank(message = "Product ID is required")
        private String productId;
        
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
        
        @Positive(message = "Price must be positive")
        private Double price;
        
        private String productName;

        // Constructors
        public OrderItem() {}

        public OrderItem(String productId, Integer quantity, Double price, String productName) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
            this.productName = productName;
        }

        // Getters and Setters
        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Double getSubtotal() {
            return price * quantity;
        }
    }
}
