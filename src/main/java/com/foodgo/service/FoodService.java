package com.foodgo.service;

import com.foodgo.model.Food;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.*;

@Service
public class FoodService {

    private static final String FILE_NAME = "foods.txt";

    @Autowired
    private FileStorageService fileStorage;

    private String filePath() {
        return fileStorage.resolve(FILE_NAME);
    }

    @PostConstruct
    public void init() throws IOException {
        File file = new File(filePath());
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public List<Food> getAllFoods() throws IOException {
        List<Food> foods = new ArrayList<>();
        File file = new File(filePath());
        if (!file.exists()) return foods;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    foods.add(Food.fromFileLine(line));
                }
            }
        }
        return foods;
    }

    public Food getFoodById(String id) throws IOException {
        for (Food f : getAllFoods()) {
            if (f.getId().equals(id)) return f;
        }
        return null;
    }

    public List<Food> getFoodsByCategory(String category) throws IOException {
        List<Food> foods = new ArrayList<>();
        for (Food f : getAllFoods()) {
            if (f.getCategory().equalsIgnoreCase(category)) {
                foods.add(f);
            }
        }
        return foods;
    }

    public void saveFood(Food food) throws IOException {
        if (food.getId() == null || food.getId().isEmpty()) {
            food.setId(String.valueOf(System.currentTimeMillis()));
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath(), true))) {
            writer.write(food.toFileLine());
            writer.newLine();
        }
    }

    public boolean updateFood(String id, Food updatedFood) throws IOException {
        List<Food> foods = getAllFoods();
        boolean found = false;
        for (Food f : foods) {
            if (f.getId().equals(id)) {
                f.setName(updatedFood.getName());
                f.setDescription(updatedFood.getDescription());
                f.setCategory(updatedFood.getCategory());
                f.setPrice(updatedFood.getPrice());
                f.setRating(updatedFood.getRating());
                f.setReviews(updatedFood.getReviews());
                f.setImage(updatedFood.getImage());
                f.setAvailable(updatedFood.isAvailable());
                found = true;
                break;
            }
        }
        if (found) saveAllFoods(foods);
        return found;
    }

    public boolean deleteFood(String id) throws IOException {
        List<Food> foods = getAllFoods();
        boolean removed = foods.removeIf(f -> f.getId().equals(id));
        if (removed) saveAllFoods(foods);
        return removed;
    }

    private void saveAllFoods(List<Food> foods) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath(), false))) {
            for (Food f : foods) {
                writer.write(f.toFileLine());
                writer.newLine();
            }
        }
    }
}
