package com.example.orderservice.controller;

import com.example.orderservice.model.Order;
import com.example.orderservice.service.OrderService;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "order-service");
        response.put("timestamp", LocalDateTime.now().toString() + "Z");
        response.put("uptime", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrders() {
        try {
            logger.info("GET /orders - Fetching all orders");
            List<Order> orders = orderService.getAllOrders();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("count", orders.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching orders: {}", e.getMessage());
            return createErrorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable String orderId) {
        try {
            logger.info("GET /orders/{} - Fetching order by ID", orderId);
            Optional<Order> order = orderService.getOrderById(orderId);
            
            if (order.isEmpty()) {
                return createErrorResponse("Order not found", HttpStatus.NOT_FOUND);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", order.get());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching order {}: {}", orderId, e.getMessage());
            return createErrorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getOrdersByUserId(@PathVariable String userId) {
        try {
            logger.info("GET /orders/user/{} - Fetching orders for user", userId);
            List<Order> orders = orderService.getOrdersByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("count", orders.size());
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching orders for user {}: {}", userId, e.getMessage());
            return createErrorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@Valid @RequestBody Order order) {
        try {
            logger.info("POST /orders - Creating new order for user: {}", order.getUserId());
            
            Order createdOrder = orderService.createOrder(order);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order created successfully");
            response.put("data", createdOrder);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating order: {}", e.getMessage());
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error creating order: {}", e.getMessage());
            return createErrorResponse("Failed to create order: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable String orderId, 
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String newStatus = statusUpdate.get("status");
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return createErrorResponse("Status is required", HttpStatus.BAD_REQUEST);
            }
            
            logger.info("PUT /orders/{}/status - Updating status to: {}", orderId, newStatus);
            Optional<Order> updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
            
            if (updatedOrder.isEmpty()) {
                return createErrorResponse("Order not found", HttpStatus.NOT_FOUND);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order status updated successfully");
            response.put("data", updatedOrder.get());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating order status for {}: {}", orderId, e.getMessage());
            return createErrorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now().toString() + "Z");
        
        return ResponseEntity.status(status).body(response);
    }
}
