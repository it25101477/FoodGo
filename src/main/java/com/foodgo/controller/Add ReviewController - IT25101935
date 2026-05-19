package com.foodgo.controller;

import com.foodgo.model.Review;
import com.foodgo.service.ReviewService;
import com.foodgo.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse> addReview(@RequestBody Review review) {
        try {
            if (review.getFoodId() == null || review.getFoodId().trim().isEmpty())
                return ResponseEntity.badRequest().body(ApiResponse.error("Food ID is required"));
            if (review.getRating() < 1 || review.getRating() > 5)
                return ResponseEntity.badRequest().body(ApiResponse.error("Rating must be between 1 and 5"));
            reviewService.addReview(review);
            Map<String,Object> data = new HashMap<>();
            data.put("reviewId", review.getId());
            data.put("rating", review.getRating());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Review added successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add review: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllReviews() {
        try {
            List<Review> reviews = reviewService.getAllReviews();
            Map<String,Object> data = new HashMap<>();
            data.put("count", reviews.size());
            data.put("reviews", reviews);
            return ResponseEntity.ok(ApiResponse.success("All reviews retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/food/{foodId}")
    public ResponseEntity<ApiResponse> getReviewsByFood(@PathVariable String foodId) {
        try {
            List<Review> reviews = reviewService.getReviewsByFoodId(foodId);
            Map<String,Object> data = new HashMap<>();
            data.put("foodId", foodId);
            data.put("reviews", reviews);
            data.put("averageRating", reviewService.getAverageRating(foodId));
            data.put("totalReviews", reviewService.getReviewCount(foodId));
            return ResponseEntity.ok(ApiResponse.success("Food reviews retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getReviewsByUser(@PathVariable String userId) {
        try {
            List<Review> reviews = reviewService.getReviewsByUserId(userId);
            Map<String,Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("count", reviews.size());
            data.put("reviews", reviews);
            return ResponseEntity.ok(ApiResponse.success("User reviews retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // Delete review — user can delete own, admin can delete any
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteReview(
            @PathVariable String id,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false, defaultValue = "false") boolean isAdmin) {
        try {
            boolean ok = reviewService.deleteReview(id, userId != null ? userId : "", isAdmin);
            if (!ok) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Cannot delete review"));
            return ResponseEntity.ok(ApiResponse.success("Review deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // Admin — hide/unhide review
    @PostMapping("/{id}/toggle-hide")
    public ResponseEntity<ApiResponse> toggleHide(@PathVariable String id) {
        try {
            boolean ok = reviewService.toggleHide(id);
            if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Review not found"));
            return ResponseEntity.ok(ApiResponse.success("Review visibility toggled"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }
}
