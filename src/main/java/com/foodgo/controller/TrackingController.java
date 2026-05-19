package com.foodgo.controller;

import com.foodgo.model.DeliveryTracking;
import com.foodgo.service.DeliveryTrackingService;
import com.foodgo.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Delivery Tracking Controller - Handles order delivery tracking HTTP requests
 */
@RestController
@RequestMapping("/api/tracking")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TrackingController {

    @Autowired
    private DeliveryTrackingService trackingService;

    @PostMapping
    public ResponseEntity<ApiResponse> createTracking(@RequestBody DeliveryTracking tracking) {
        try {
            if (tracking.getOrderId() == null || tracking.getOrderId().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Order ID is required"));
            }
            if (tracking.getRiderId() == null || tracking.getRiderId().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Rider ID is required"));
            }

            String trackingId = trackingService.createTracking(tracking);
            Map<String, Object> data = new HashMap<>();
            data.put("trackingId", trackingId);
            data.put("orderId", tracking.getOrderId());
            data.put("status", "picked");
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tracking created successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create tracking: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllTrackings() {
        try {
            List<DeliveryTracking> trackings = trackingService.getAllTrackings();
            Map<String, Object> data = new HashMap<>();
            data.put("count", trackings.size());
            data.put("trackings", trackings);
            
            return ResponseEntity.ok(ApiResponse.success("All trackings retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch trackings: " + e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse> getTrackingByOrder(@PathVariable String orderId) {
        try {
            DeliveryTracking tracking = trackingService.getTrackingByOrderId(orderId);
            if (tracking != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("tracking", tracking);
                return ResponseEntity.ok(ApiResponse.success("Tracking retrieved", data));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Tracking not found for this order"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch tracking: " + e.getMessage()));
        }
    }

    @GetMapping("/rider/{riderId}")
    public ResponseEntity<ApiResponse> getTrackingByRider(@PathVariable String riderId) {
        try {
            List<DeliveryTracking> trackings = trackingService.getTrackingByRiderId(riderId);
            Map<String, Object> data = new HashMap<>();
            data.put("riderId", riderId);
            data.put("count", trackings.size());
            data.put("trackings", trackings);
            
            return ResponseEntity.ok(ApiResponse.success("Rider trackings retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch rider trackings: " + e.getMessage()));
        }
    }

    @PutMapping("/{trackingId}/status")
    public ResponseEntity<ApiResponse> updateTrackingStatus(
            @PathVariable String trackingId,
            @RequestBody Map<String, String> body) {
        try {
            String newStatus = body.get("status");
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Status is required"));
            }

            boolean result = trackingService.updateTrackingStatus(trackingId, newStatus);
            if (result) {
                Map<String, Object> data = new HashMap<>();
                data.put("trackingId", trackingId);
                data.put("status", newStatus);
                return ResponseEntity.ok(ApiResponse.success("Tracking status updated", data));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Tracking not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update tracking status: " + e.getMessage()));
        }
    }

    @PutMapping("/{trackingId}/location")
    public ResponseEntity<ApiResponse> updateTrackingLocation(
            @PathVariable String trackingId,
            @RequestBody Map<String, Double> body) {
        try {
            Double latitude = body.get("latitude");
            Double longitude = body.get("longitude");

            if (latitude == null || longitude == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Latitude and longitude are required"));
            }

            boolean result = trackingService.updateTrackingLocation(trackingId, latitude, longitude);
            if (!result) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Tracking not found"));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("trackingId", trackingId);
            data.put("latitude", latitude);
            data.put("longitude", longitude);

            return ResponseEntity.ok(ApiResponse.success("Location updated successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update location: " + e.getMessage()));
        }
    }
}
