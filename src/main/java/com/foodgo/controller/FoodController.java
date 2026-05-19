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
@RestController //
@RequestMapping("/api/foods") //sets the base URL for the all end point
@CrossOrigin(origins = "*", allowedHeaders = "*") //enables CORS for all domains
public class FoodController {

    @Autowired
    private FoodService foodService;

    @GetMapping  // get all foods
    public ResponseEntity<ApiResponse> getAllFoods() {//method returns HTTP response with ApiResponse  object
        try {
            List<Food> foods = foodService.getAllFoods();
            Map<String, Object> data = new HashMap<>();//creates response data map
            data.put("count", foods.size());
            data.put("foods", foods);
            return ResponseEntity.ok(ApiResponse.success("Foods retrieved successfully", data));
        } catch (Exception e) { //catch runtime errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch foods: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")                           //extract ID from URL
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

    @PostMapping //create
    public ResponseEntity<ApiResponse> createFood(@RequestBody Food food) {
        try {
            if (food.getName() == null || food.getName().trim().isEmpty()) {  //Validation check
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Food name is required"));
            }
            food.setId(String.valueOf(System.currentTimeMillis()));//generates unique ID using timestamp
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
//update food
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateFood(@PathVariable String id, @RequestBody Food updatedFood) {
        try {
            boolean result = foodService.updateFood(id, updatedFood);//calls update service method
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
