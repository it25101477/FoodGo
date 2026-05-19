package com.foodgo.controller;

import com.foodgo.model.Payment;
import com.foodgo.model.Rider;
import com.foodgo.model.Review;
import com.foodgo.service.PaymentService;
import com.foodgo.service.ReviewService;
import com.foodgo.service.RiderService;
import com.foodgo.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AdminController {

    @Value("${foodgo.admin.username:admin}")
    private String adminUsername;

    @Value("${foodgo.admin.password:admin123}")
    private String adminPassword;

    @Autowired
    private RiderService riderService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody Map<String,String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        if (username == null || password == null)
            return ResponseEntity.badRequest().body(ApiResponse.error("Username and password are required"));
        if (adminUsername.equals(username.trim()) && adminPassword.equals(password)) {
            Map<String,Object> data = new HashMap<>();
            data.put("username", adminUsername); data.put("role", "admin");
            return ResponseEntity.ok(ApiResponse.success("Admin login successful", data));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid admin credentials"));
    }

    // ── Rider Verification Dashboard ──
    @GetMapping("/riders")
    public ResponseEntity<ApiResponse> getAllRiders() {
        try {
            List<Rider> riders = riderService.getAllRiders();
            Map<String,Object> data = new HashMap<>();
            data.put("total", riders.size());
            data.put("riders", riders);
            long pending  = riders.stream().filter(r -> "pending".equals(r.getApprovalStatus())).count();
            long approved = riders.stream().filter(r -> "approved".equals(r.getApprovalStatus())).count();
            long rejected = riders.stream().filter(r -> "rejected".equals(r.getApprovalStatus())).count();
            data.put("pendingCount", pending);
            data.put("approvedCount", approved);
            data.put("rejectedCount", rejected);
            return ResponseEntity.ok(ApiResponse.success("Riders retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/riders/pending")
    public ResponseEntity<ApiResponse> getPendingRiders() {
        try {
            List<Rider> riders = riderService.getPendingRiders();
            Map<String,Object> data = new HashMap<>();
            data.put("count", riders.size());
            data.put("riders", riders);
            return ResponseEntity.ok(ApiResponse.success("Pending riders retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/riders/{id}/approve")
    public ResponseEntity<ApiResponse> approveRider(@PathVariable String id) {
        try {
            String result = riderService.approveRider(id);
            if ("approved".equals(result)) return ResponseEntity.ok(ApiResponse.success("Rider approved"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Rider not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/riders/{id}/reject")
    public ResponseEntity<ApiResponse> rejectRider(@PathVariable String id, @RequestBody Map<String,String> body) {
        try {
            String reason = body.getOrDefault("reason", "Documents not acceptable");
            String result = riderService.rejectRider(id, reason);
            if ("rejected".equals(result)) return ResponseEntity.ok(ApiResponse.success("Rider rejected"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Rider not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Review Moderation ──
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse> getAllReviews() {
        try {
            List<Review> reviews = reviewService.getAllReviews();
            Map<String,Object> data = new HashMap<>();
            data.put("count", reviews.size());
            data.put("reviews", reviews);
            return ResponseEntity.ok(ApiResponse.success("Reviews retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<ApiResponse> deleteReview(@PathVariable String id) {
        try {
            boolean ok = reviewService.deleteReview(id, null, true);
            if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Review not found"));
            return ResponseEntity.ok(ApiResponse.success("Review deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reviews/{id}/toggle-hide")
    public ResponseEntity<ApiResponse> toggleHideReview(@PathVariable String id) {
        try {
            boolean ok = reviewService.toggleHide(id);
            if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Review not found"));
            return ResponseEntity.ok(ApiResponse.success("Review visibility toggled"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Payments ──
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse> getAllPayments() {
        try {
            List<Payment> payments = paymentService.getAllPayments();
            Map<String,Object> data = new HashMap<>();
            data.put("count", payments.size());
            data.put("payments", payments);
            double totalRevenue = payments.stream().filter(p -> "paid".equals(p.getStatus())).mapToDouble(Payment::getAmount).sum();
            data.put("totalRevenue", totalRevenue);
            return ResponseEntity.ok(ApiResponse.success("Payments retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/payments/{id}/status")
    public ResponseEntity<ApiResponse> updatePaymentStatus(@PathVariable String id, @RequestBody Map<String,String> body) {
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
}
