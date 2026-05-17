package com.foodgo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User Model - Represents a customer in the FoodGo system
 * This is OOP: Encapsulation — data is private, accessed via getters/setters
 */
public class User {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    @JsonProperty("phone")
    private String phone;

    // Constructor (used to create a new User object)
    public User(String id, String name, String email, String password, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    // Empty constructor (needed for JSON deserialization)
    public User() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // Converts a User object to a line of text for saving to file
    // Format: id,name,email,password,phone
    public String toFileLine() {
        return id + "," + name + "," + email + "," + password + "," + phone;
    }

    // Converts a line of text from file back to a User object
    public static User fromFileLine(String line) {
        String[] parts = line.split(",", 5);
        String id       = parts.length > 0 ? parts[0] : "";
        String name     = parts.length > 1 ? parts[1] : "";
        String email    = parts.length > 2 ? parts[2] : "";
        String password = parts.length > 3 ? parts[3] : "";
        String phone    = parts.length > 4 ? parts[4] : "";
        return new User(id, name, email, password, phone);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}