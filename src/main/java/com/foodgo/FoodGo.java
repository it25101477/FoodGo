package com.foodgo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * FoodGo Application - Main Entry Point
 * 
 * A Spring Boot REST API for food delivery service management
 * Handles user (customer) and rider (delivery partner) operations
 * 
 * Access the application at: http://localhost:8080
 * API Base URL: http://localhost:8080/api
 * 
 * @author FoodGo Dev Team
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.foodgo"})
public class FoodGo {

    public static void main(String[] args) {
        SpringApplication.run(FoodGo.class, args);
        
        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║         FoodGo - Food Delivery Backend               ║");
        System.out.println("║                   Started Successfully               ║");
        System.out.println("╠════════════════════════════════════════════════════  ╣");
        System.out.println("║ Customer:  http://localhost:8080/                  ║");
        System.out.println("║ Rider:     http://localhost:8080/rider             ║");
        System.out.println("║ Admin:     http://localhost:8080/admin             ║");
        System.out.println("║ API:       http://localhost:8080/api               ║");
        System.out.println("║ Admin login: admin / admin123                        ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");
    }
}
