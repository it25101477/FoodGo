package com.foodgo.controller;

import com.foodgo.model.Food;
import com.foodgo.service.FoodService;
import com.foodgo.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Food Controller - Handles all food/menu related HTTP requests
 */
@RestController
@RequestMapping("/api/foods")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FoodController {

    @Autowired
    private FoodService foodService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllFoods() {
        try {
            List<Food> foods = foodService.getAllFoods();
            Map<String, Object> data = new HashMap<>();
            data.put("count", foods.size());
            data.put("foods", foods);
            return ResponseEntity.ok(ApiResponse.success("Foods retrieved successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch foods: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getFoodById(@PathVariable String id) {
        try {
            Food food = foodService.getFoodById(id);
            if (food != null) {
                return ResponseEntity.ok(ApiResponse.success("Food found", food));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Food not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse> getFoodsByCategory(@PathVariable String category) {
        try {
            List<Food> foods = foodService.getFoodsByCategory(category);
            Map<String, Object> data = new HashMap<>();
            data.put("category", category);
            data.put("count", foods.size());
            data.put("foods", foods);
            return ResponseEntity.ok(ApiResponse.success("Foods in category retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch foods by category: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createFood(@RequestBody Food food) {
        try {
            if (food.getName() == null || food.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Food name is required"));
            }
            food.setId(String.valueOf(System.currentTimeMillis()));
            foodService.saveFood(food);
            Map<String, Object> data = new HashMap<>();
            data.put("foodId", food.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Food created successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create food: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateFood(@PathVariable String id, @RequestBody Food updatedFood) {
        try {
            boolean result = foodService.updateFood(id, updatedFood);
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("Food updated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Food not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Update failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteFood(@PathVariable String id) {
        try {
            boolean result = foodService.deleteFood(id);
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("Food deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Food not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Delete failed: " + e.getMessage()));
        }
    }
}
