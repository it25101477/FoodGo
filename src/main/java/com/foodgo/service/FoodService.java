package com.foodgo.service;

import com.foodgo.model.Food;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; //mark this class as a service class

import jakarta.annotation.PostConstruct; //run a method auto after object creation
import java.io.*;
import java.util.*;  // import file handling and collection classes

@Service
public class FoodService {   //create service class as food service
// stores the text file name
    private static final String FILE_NAME = "foods.txt";

    @Autowired  // dependency injection
    private FileStorageService fileStorage;
// gets the full file path
    private String filePath() {
        return fileStorage.resolve(FILE_NAME);
    }

    @PostConstruct
    public void init() throws IOException { //Runs auto after service starts
        File file = new File(filePath());  // create a file object
        file.getParentFile().mkdirs(); //create parent folfer if missing
        if (!file.exists()) {
            file.createNewFile();//create a file if doesnt exist
        }
    }
// return all foods from the file   //abstraction
    public List<Food> getAllFoods() throws IOException {
        List<Food> foods = new ArrayList<>();//create an empty list
        File file = new File(filePath());
        if (!file.exists()) return foods;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) { // reads the file line by line
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    foods.add(Food.fromFileLine(line)); //text line converts to in food object and add to list
                }
            }
        }
        return foods;
    }
//finds food using ID
    public Food getFoodById(String id) throws IOException {
        for (Food f : getAllFoods()) {
            if (f.getId().equals(id)) return f; //CHECK ID is match or not
        }
        return null;
    }
// get food by category
    public List<Food> getFoodsByCategory(String category) throws IOException {
        List<Food> foods = new ArrayList<>();
        for (Food f : getAllFoods()) {
            if (f.getCategory().equalsIgnoreCase(category)) { //compares uppercase and lowercase
                foods.add(f);
            }
        }
        return foods;
    }
//save a new food item
    public void saveFood(Food food) throws IOException {
        if (food.getId() == null || food.getId().isEmpty()) {
            food.setId(String.valueOf(System.currentTimeMillis()));//generate a unique ID using current time
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath(), true))) {  // open file in append mode
            writer.write(food.toFileLine());  //write food data into file
            writer.newLine();
        }
    }
//update food details
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
                found = true; //mark that food was found
                break;
            }
        }
        if (found) saveAllFoods(foods);//update list save to the file
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
