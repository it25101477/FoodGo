package com.foodgo.controller;

import com.foodgo.model.Payment;
import com.foodgo.service.PaymentService;
import com.foodgo.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse> createPayment(@RequestBody Payment payment) {
        try {
            if (payment.getOrderId() == null || payment.getOrderId().trim().isEmpty())
                return ResponseEntity.badRequest().body(ApiResponse.error("Order ID is required"));
            if (payment.getMethod() == null || (!payment.getMethod().equals("cash") && !payment.getMethod().equals("card")))
                return ResponseEntity.badRequest().body(ApiResponse.error("Payment method must be cash or card"));
            if (payment.getAmount() <= 0)
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid payment amount"));

            // Simulate card authorization
            if ("card".equals(payment.getMethod())) {
                if (payment.getCardLast4() == null || payment.getCardLast4().length() != 4)
                    return ResponseEntity.badRequest().body(ApiResponse.error("Card last 4 digits required"));
                payment.setStatus("paid");
                payment.setTransactionRef("TXN" + System.currentTimeMillis());
            } else {
                payment.setStatus("pending"); // cash – mark paid on delivery
            }

            Payment created = paymentService.createPayment(payment);
            Map<String,Object> data = new HashMap<>();
            data.put("paymentId", created.getId());
            data.put("status", created.getStatus());
            data.put("transactionRef", created.getTransactionRef());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Payment recorded", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Payment failed: " + e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse> getByOrder(@PathVariable String orderId) {
        try {
            Payment p = paymentService.getByOrderId(orderId);
            if (p == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Payment not found"));
            return ResponseEntity.ok(ApiResponse.success("Payment found", p));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getByUser(@PathVariable String userId) {
        try {
            List<Payment> payments = paymentService.getByUserId(userId);
            Map<String,Object> data = new HashMap<>();
            data.put("count", payments.size());
            data.put("payments", payments);
            return ResponseEntity.ok(ApiResponse.success("Payments retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        try {
            List<Payment> payments = paymentService.getAllPayments();
            Map<String,Object> data = new HashMap<>();
            data.put("count", payments.size());
            data.put("payments", payments);
            return ResponseEntity.ok(ApiResponse.success("All payments retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateStatus(@PathVariable String id, @RequestBody Map<String,String> body) {
        try {
            String status = body.get("status");
            if (status == null) return ResponseEntity.badRequest().body(ApiResponse.error("Status required"));
            boolean ok = paymentService.updateStatus(id, status);
            if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Payment not found"));
            return ResponseEntity.ok(ApiResponse.success("Payment status updated"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable String id) {
        try {
            boolean ok = paymentService.deletePayment(id);
            if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Payment not found"));
            return ResponseEntity.ok(ApiResponse.success("Payment deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }
}
