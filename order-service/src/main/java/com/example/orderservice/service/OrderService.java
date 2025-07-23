package com.example.orderservice.service;

import com.example.orderservice.model.Order;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${services.user-service.url:http://localhost:3001}")
    private String userServiceUrl;
    
    @Value("${services.product-service.url:http://localhost:3002}")
    private String productServiceUrl;
    
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Order> getAllOrders() {
        logger.info("Fetching all orders");
        return new ArrayList<>(orders.values());
    }

    public Optional<Order> getOrderById(String orderId) {
        logger.info("Fetching order by ID: {}", orderId);
        return Optional.ofNullable(orders.get(orderId));
    }

    public List<Order> getOrdersByUserId(String userId) {
        logger.info("Fetching orders for user: {}", userId);
        return orders.values().stream()
                .filter(order -> order.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public Order createOrder(Order order) throws Exception {
        logger.info("Creating new order for user: {}", order.getUserId());
        
        // Generate order ID
        String orderId = UUID.randomUUID().toString();
        order.setId(orderId);
        
        // Validate user exists
        if (!validateUser(order.getUserId())) {
            throw new IllegalArgumentException("User not found: " + order.getUserId());
        }
        
        // Validate products and calculate total
        double totalAmount = 0.0;
        for (Order.OrderItem item : order.getItems()) {
            JsonNode product = getProduct(item.getProductId());
            if (product == null) {
                throw new IllegalArgumentException("Product not found: " + item.getProductId());
            }
            
            // Set product name and price from product service
            item.setProductName(product.get("data").get("name").asText());
            item.setPrice(product.get("data").get("price").asDouble());
            
            // Check stock availability
            if (!checkStock(item.getProductId(), item.getQuantity())) {
                throw new IllegalArgumentException("Insufficient stock for product: " + item.getProductId());
            }
            
            // Reserve stock
            reserveStock(item.getProductId(), item.getQuantity());
            
            totalAmount += item.getSubtotal();
        }
        
        order.setTotalAmount(totalAmount);
        order.setStatus("CONFIRMED");
        
        // Store order
        orders.put(orderId, order);
        
        logger.info("Order created successfully: {}", orderId);
        return order;
    }

    public Optional<Order> updateOrderStatus(String orderId, String status) {
        logger.info("Updating order {} status to: {}", orderId, status);
        
        Order order = orders.get(orderId);
        if (order != null) {
            order.setStatus(status);
            return Optional.of(order);
        }
        return Optional.empty();
    }

    private boolean validateUser(String userId) {
        try {
            logger.info("Validating user: {}", userId);
            String url = userServiceUrl + "/users/" + userId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                return jsonResponse.get("success").asBoolean();
            }
            return false;
        } catch (Exception e) {
            logger.error("Error validating user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    private JsonNode getProduct(String productId) {
        try {
            logger.info("Fetching product: {}", productId);
            String url = productServiceUrl + "/products/" + productId;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readTree(response.getBody());
            }
            return null;
        } catch (Exception e) {
            logger.error("Error fetching product {}: {}", productId, e.getMessage());
            return null;
        }
    }

    private boolean checkStock(String productId, int quantity) {
        try {
            logger.info("Checking stock for product {} (quantity: {})", productId, quantity);
            String url = productServiceUrl + "/products/" + productId + "/stock";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                int availableStock = jsonResponse.get("data").get("stock").asInt();
                return availableStock >= quantity;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error checking stock for product {}: {}", productId, e.getMessage());
            return false;
        }
    }

    private void reserveStock(String productId, int quantity) throws Exception {
        try {
            logger.info("Reserving stock for product {} (quantity: {})", productId, quantity);
            String url = productServiceUrl + "/products/" + productId + "/reserve";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("quantity", quantity);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new Exception("Failed to reserve stock for product: " + productId);
            }
        } catch (Exception e) {
            logger.error("Error reserving stock for product {}: {}", productId, e.getMessage());
            throw e;
        }
    }
}
