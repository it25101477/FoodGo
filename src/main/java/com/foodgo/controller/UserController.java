package com.foodgo.controller;

import com.foodgo.model.User;
import com.foodgo.service.UserService;
import com.foodgo.util.ApiResponse;
import com.foodgo.util.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User Controller - Handles all user-related HTTP requests
 * Provides both Web views (Thymeleaf) and REST API endpoints
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    @Autowired
    private UserService userService;

    // ═══════════════════════════════════════════════════════════════
    // REST API ENDPOINTS (JSON)
    // ═══════════════════════════════════════════════════════════════

    @PostMapping("/api/users/register")
    public ResponseEntity<ApiResponse> registerRest(@RequestBody User user) {
        try {
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email is required"));
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Password is required"));
            }
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Name is required"));
            }

            String result = userService.registerUser(user);
            if ("success".equals(result)) {
                Map<String, Object> data = new HashMap<>();
                data.put("userId", user.getId());
                data.put("email", user.getEmail());
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("User registered successfully", data));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(result));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/api/users/login")
    public ResponseEntity<ApiResponse> loginRest(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email and password are required"));
            }

            User user = userService.loginUser(email, password);
            if (user != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("name", user.getName());
                userData.put("email", user.getEmail());
                userData.put("phone", user.getPhone());
                return ResponseEntity.ok(ApiResponse.success("Login successful", userData));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid email or password"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    @GetMapping("/api/users")
    public ResponseEntity<ApiResponse> getAllUsersRest() {
        try {
            List<User> users = userService.getAllUsers();
            Map<String, Object> data = new HashMap<>();
            data.put("count", users.size());
            data.put("users", UserProfile.fromUsers(users));
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch users: " + e.getMessage()));
        }
    }

    @GetMapping("/api/users/{id}")
    public ResponseEntity<ApiResponse> getUserByIdRest(@PathVariable String id) {
        try {
            User user = userService.getUserById(id);
            if (user != null) {
                return ResponseEntity.ok(ApiResponse.success("User found", UserProfile.fromUser(user)));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    @PutMapping("/api/users/{id}")
    public ResponseEntity<ApiResponse> updateUserRest(@PathVariable String id, @RequestBody User updatedUser) {
        try {
            User existing = userService.getUserById(id);
            if (existing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found"));
            }

            boolean result = userService.updateUser(id, updatedUser.getName(), updatedUser.getPhone());
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("User updated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Update failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<ApiResponse> deleteUserRest(@PathVariable String id) {
        try {
            boolean result = userService.deleteUser(id);
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Delete failed: " + e.getMessage()));
        }
    }

    @PostMapping("/api/users/{id}/change-password")
    public ResponseEntity<ApiResponse> changePasswordRest(
            @PathVariable String id,
            @RequestBody Map<String, String> passwordData) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found"));
            }

            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Current password and new password are required"));
            }

            String result = userService.updatePassword(user.getEmail(), currentPassword, newPassword);
            if ("updated".equals(result)) {
                return ResponseEntity.ok(ApiResponse.success("Password updated successfully"));
            } else if ("wrong_password".equals(result)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Current password is incorrect"));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(result));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Password change failed: " + e.getMessage()));
        }
    }


    @PostMapping("/api/users/{id}/change-email")
    public ResponseEntity<ApiResponse> changeEmailRest(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found"));
            }
            String newEmail = body.get("newEmail");
            String password = body.get("password");
            if (newEmail == null || newEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("New email is required"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Password confirmation required"));
            }
            // Re-authenticate before sensitive change
            if (!userService.loginUser(user.getEmail(), password).equals(user)) {
                // loginUser returns null on fail
            }
            User authenticated = userService.loginUser(user.getEmail(), password);
            if (authenticated == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Incorrect password"));
            }
            // Basic email validation
            if (!newEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid email format"));
            }
            String result = userService.updateEmail(id, newEmail);
            switch (result) {
                case "updated":
                    return ResponseEntity.ok(ApiResponse.success("Email updated successfully"));
                case "email_taken":
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Email already in use by another account"));
                default:
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Email update failed: " + e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // LEGACY FORM-BASED ENDPOINTS (backward compatibility)
    // ═══════════════════════════════════════════════════════════════

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String phone) throws Exception {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setPhone(phone);
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password) throws Exception {
        User user = userService.loginUser(email, password);
        if (user != null) {
            return "success:" + user.getName() + ":" + user.getId() + ":" + user.getPhone();
        }
        return "fail";
    }

    @PostMapping("/deleteUser")
    public String deleteUser(@RequestParam(required = false) String id,
                             @RequestParam(required = false) String email) throws Exception {
        boolean result = false;
        if (email != null && !email.isEmpty()) {
            result = userService.deleteUserByEmail(email);
        } else if (id != null && !id.isEmpty()) {
            result = userService.deleteUser(id);
        }
        return result ? "deleted" : "not found";
    }

    @PostMapping("/updateUser")
    public String updateUser(@RequestParam(required = false) String id,
                             @RequestParam(required = false) String email,
                             @RequestParam String name,
                             @RequestParam String phone) throws Exception {
        boolean result = false;
        if (email != null && !email.isEmpty()) {
            result = userService.updateUserByEmail(email, name, phone);
        } else if (id != null && !id.isEmpty()) {
            result = userService.updateUser(id, name, phone);
        }
        return result ? "updated" : "not found";
    }

    @GetMapping("/getUsers")
    public List<User> getUsers() throws Exception {
        return userService.getAllUsers();
    }

    @PostMapping("/updatePassword")
    public String updatePassword(@RequestParam String email,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword) throws Exception {
        return userService.updatePassword(email, currentPassword, newPassword);
    }
}
