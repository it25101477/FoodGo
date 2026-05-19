package com.foodgo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Food Model - Represents a menu item in the FoodGo system
 */
public class Food {
    @JsonProperty("id")   //JSON Field to Java Field
    private String id;    // Attributes    and Encapsulation
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("price")
    private double price;
    
    @JsonProperty("rating")
    private double rating;
    
    @JsonProperty("reviews")
    private int reviews;
    
    @JsonProperty("image")
    private String image;
    
    @JsonProperty("available")
    private boolean available;

    public Food() {}  //Default contructor .Create an empty Food object.

    public Food(String id, String name, String description, String category, double price, 
                double rating, int reviews, String image, boolean available) {   // parameterized constructor used to create a food object with values
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.rating = rating;
        this.reviews = reviews;
        this.image = image;
        this.available = available;
    }
// //getters and setters   g-read s-update
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
                  //Abstraction
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviews() { return reviews; }
    public void setReviews(int reviews) { this.reviews = reviews; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
// object data convert in to one text line
    public String toFileLine() {
        return id + "|" + name + "|" + description + "|" + category + "|" + price + "|" + 
               rating + "|" + reviews + "|" + image + "|" + available;
    }
//one text line convert in to object data
    public static Food fromFileLine(String line) {
        String[] parts = line.split("\\|", -1);//split the line using
        return new Food(  //create and return a new food object using split values  and//new object create
            parts.length > 0 ? parts[0] : "",
            parts.length > 1 ? parts[1] : "",
            parts.length > 2 ? parts[2] : "",
            parts.length > 3 ? parts[3] : "",
            parts.length > 4 ? Double.parseDouble(parts[4]) : 0,
            parts.length > 5 ? Double.parseDouble(parts[5]) : 0,
            parts.length > 6 ? Integer.parseInt(parts[6]) : 0,
            parts.length > 7 ? parts[7] : "",
            parts.length > 8 ? Boolean.parseBoolean(parts[8]) : true
        );
    }

    @Override  // method overriding
    public String toString() { //return object details to readable text
        return "Food{" + "id='" + id + "', name='" + name + "', price=" + price + 
               ", rating=" + rating + ", available=" + available + '}';
    }
}
