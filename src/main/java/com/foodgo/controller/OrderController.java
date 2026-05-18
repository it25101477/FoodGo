package com.foodgo.controller;

import com.foodgo.model.Order;
import com.foodgo.service.OrderService;
import com.foodgo.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(@RequestBody Order order) {
        try {
            if (order.getUserId() == null || order.getUserId().trim().isEmpty())
                return ResponseEntity.badRequest().body(ApiResponse.error("User ID is required"));
            if (order.getItems() == null || order.getItems().isEmpty())
                return ResponseEntity.badRequest().body(ApiResponse.error("Order items cannot be empty"));

            String orderId = orderService.createOrder(order);
            Map<String,Object> data = new HashMap<>();
            data.put("orderId",    orderId);
            data.put("status",     order.getStatus() != null ? order.getStatus() : "pending");
            data.put("total",      order.getTotal());
            data.put("createdAt",  order.getCreatedAt());
            data.put("paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod() : "cod");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Order created successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create order: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            // Sort newest first
            orders.sort((a,b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
            Map<String,Object> data = new HashMap<>();
            data.put("count",  orders.size());
            data.put("orders", orders);
            return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch orders: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable String id) {
        try {
            Order order = orderService.getOrderById(id);
            if (order != null) return ResponseEntity.ok(ApiResponse.success("Order found", order));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Order not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getOrdersByUserId(@PathVariable String userId) {
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            Map<String,Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("count",  orders.size());
            data.put("orders", orders);
            return ResponseEntity.ok(ApiResponse.success("User orders retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse> getAvailableOrders() {
        try {
            List<Order> orders = orderService.getAvailableOrders();
            Map<String,Object> data = new HashMap<>();
            data.put("count",  orders.size());
            data.put("orders", orders);
            return ResponseEntity.ok(ApiResponse.success("Available orders retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/rider/{riderId}")
    public ResponseEntity<ApiResponse> getOrdersByRiderId(@PathVariable String riderId) {
        try {
            List<Order> orders = orderService.getOrdersByRiderId(riderId);
            Map<String,Object> data = new HashMap<>();
            data.put("riderId", riderId);
            data.put("count",   orders.size());
            data.put("orders",  orders);
            return ResponseEntity.ok(ApiResponse.success("Rider orders retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/accept")
    public ResponseEntity<ApiResponse> acceptOrder(@PathVariable String orderId,
                                                    @RequestBody Map<String,String> body) {
        try {
            String riderId = body.get("riderId");
            if (riderId == null || riderId.trim().isEmpty())
                return ResponseEntity.badRequest().body(ApiResponse.error("Rider ID is required"));
            boolean result = orderService.assignRider(orderId, riderId);
            if (result) return ResponseEntity.ok(ApiResponse.success("Order accepted successfully"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Order not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse> getOrdersByStatus(@PathVariable String status) {
        try {
            List<Order> orders = orderService.getOrdersByStatus(status);
            Map<String,Object> data = new HashMap<>();
            data.put("status", status);
            data.put("count",  orders.size());
            data.put("orders", orders);
            return ResponseEntity.ok(ApiResponse.success("Orders retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(@PathVariable String id,
                                                          @RequestBody Map<String,String> body) {
        try {
            String newStatus = body.get("status");
            if (newStatus == null || newStatus.trim().isEmpty())
                return ResponseEntity.badRequest().body(ApiResponse.error("Status is required"));
            boolean result = orderService.updateOrderStatus(id, newStatus);
            if (result) return ResponseEntity.ok(ApiResponse.success("Order status updated to " + newStatus));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Order not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteOrder(@PathVariable String id) {
        try {
            boolean result = orderService.deleteOrder(id);
            if (result) return ResponseEntity.ok(ApiResponse.success("Order deleted"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Order not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/assign-rider/{riderId}")
    public ResponseEntity<ApiResponse> assignRider(@PathVariable String orderId,
                                                     @PathVariable String riderId) {
        try {
            boolean result = orderService.assignRider(orderId, riderId);
            if (result) return ResponseEntity.ok(ApiResponse.success("Rider assigned successfully"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Order not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Payments endpoint (served from order data) ───────────────────────────
    @GetMapping("/payments/all")
    public ResponseEntity<ApiResponse> getAllPayments() {
        try {
            List<Map<String,String>> payments = orderService.getAllPayments();
            double totalRevenue  = 0;
            double codTotal      = 0;
            double cardTotal     = 0;
            for (Map<String,String> p : payments) {
                double amt = 0;
                try { amt = Double.parseDouble(p.getOrDefault("amount","0")); } catch(Exception ignored) {}
                if ("paid".equals(p.get("status"))) totalRevenue += amt;
                if ("cod".equals(p.get("method")) || "cash".equals(p.get("method"))) codTotal += amt;
                else cardTotal += amt;
            }
            Map<String,Object> data = new HashMap<>();
            data.put("count",        payments.size());
            data.put("payments",     payments);
            data.put("totalRevenue", totalRevenue);
            data.put("codTotal",     codTotal);
            data.put("cardTotal",    cardTotal);
            return ResponseEntity.ok(ApiResponse.success("Payments retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }
}
