package com.foodgo.controller;

import com.foodgo.model.Rider;
import com.foodgo.service.RiderService;
import com.foodgo.util.ApiResponse;
import com.foodgo.util.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RiderController {

    @Autowired
    private RiderService riderService;

    // ── Register ──
    @PostMapping("/api/riders/register")
    public ResponseEntity<ApiResponse> registerRest(@RequestBody Rider rider) {
        try {
            if (rider.getEmail() == null || rider.getEmail().trim().isEmpty())
                return ResponseEntity.badRequest().body(ApiResponse.error("Email is required"));
            if (rider.getPassword() == null || rider.getPassword().length() < 6)
                return ResponseEntity.badRequest().body(ApiResponse.error("Password must be at least 6 characters"));
            if (rider.getName() == null || rider.getName().trim().isEmpty())
                return ResponseEntity.badRequest().body(ApiResponse.error("Name is required"));
            if (rider.getPhone() == null || rider.getPhone().trim().isEmpty())
                return ResponseEntity.badRequest().body(ApiResponse.error("Phone number is required"));

            String result = riderService.registerRider(rider);
            if ("success".equals(result)) {
                Map<String,Object> data = new HashMap<>();
                data.put("riderId", rider.getId());
                data.put("email", rider.getEmail());
                data.put("approvalStatus", rider.getApprovalStatus());
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Rider registered successfully. Please upload vehicle & documents.", data));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    // ── Login ──
    @PostMapping("/api/riders/login")
    public ResponseEntity<ApiResponse> loginRest(@RequestBody Map<String,String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty())
                return ResponseEntity.badRequest().body(ApiResponse.error("Email and password are required"));

            Rider rider = riderService.loginRider(email, password);
            if (rider != null) {
                Map<String,Object> data = new HashMap<>();
                data.put("id", rider.getId());
                data.put("name", rider.getName());
                data.put("email", rider.getEmail());
                data.put("phone", rider.getPhone());
                data.put("online", rider.isOnline());
                data.put("approvalStatus", rider.getApprovalStatus());
                data.put("rejectionReason", rider.getRejectionReason());
                data.put("canGoOnline", rider.canGoOnline());
                data.put("vehicleDetailsSubmitted", rider.isVehicleDetailsSubmitted());
                data.put("documentsSubmitted", rider.isDocumentsSubmitted());
                data.put("bikeModel", rider.getBikeModel());
                data.put("bikeNumber", rider.getBikeNumber());
                return ResponseEntity.ok(ApiResponse.success("Login successful", data));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Invalid email or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    // ── Get All ──
    @GetMapping("/api/riders")
    public ResponseEntity<ApiResponse> getAllRidersRest() {
        try {
            List<Rider> riders = riderService.getAllRiders();
            Map<String,Object> data = new HashMap<>();
            data.put("count", riders.size());
            data.put("riders", riders);
            return ResponseEntity.ok(ApiResponse.success("Riders retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/api/riders/pending")
    public ResponseEntity<ApiResponse> getPendingRiders() {
        try {
            List<Rider> riders = riderService.getPendingRiders();
            Map<String,Object> data = new HashMap<>();
            data.put("count", riders.size());
            data.put("riders", riders);
            return ResponseEntity.ok(ApiResponse.success("Pending riders retrieved", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Get by ID ──
    @GetMapping("/api/riders/{id}")
    public ResponseEntity<ApiResponse> getRiderByIdRest(@PathVariable String id) {
        try {
            Rider rider = riderService.getRiderById(id);
            if (rider == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Rider not found"));
            return ResponseEntity.ok(ApiResponse.success("Rider found", rider));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Update profile (name + phone) ──
    @PutMapping("/api/riders/{id}")
    public ResponseEntity<ApiResponse> updateRiderRest(@PathVariable String id, @RequestBody Rider updatedRider) {
        try {
            Rider existing = riderService.getRiderById(id);
            if (existing == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Rider not found"));
            if (updatedRider.getName() == null || updatedRider.getName().trim().isEmpty())
                return ResponseEntity.badRequest().body(ApiResponse.error("Name is required"));
            boolean ok = riderService.updateRiderById(id, updatedRider.getName().trim(), updatedRider.getPhone() != null ? updatedRider.getPhone().trim() : existing.getPhone());
            if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Rider not found"));
            Rider updated = riderService.getRiderById(id);
            Map<String,Object> data = new HashMap<>();
            data.put("name", updated.getName()); data.put("phone", updated.getPhone());
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Delete ──
    @DeleteMapping("/api/riders/{id}")
    public ResponseEntity<ApiResponse> deleteRiderRest(@PathVariable String id) {
        try {
            boolean ok = riderService.deleteRiderById(id);
            if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Rider not found"));
            return ResponseEntity.ok(ApiResponse.success("Rider deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Change Password ──
    @PostMapping("/api/riders/{id}/change-password")
    public ResponseEntity<ApiResponse> changePasswordRest(@PathVariable String id, @RequestBody Map<String,String> body) {
        try {
            Rider rider = riderService.getRiderById(id);
            if (rider == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Rider not found"));
            String currentPassword = body.get("currentPassword");
            String newPassword = body.get("newPassword");
            if (currentPassword == null || newPassword == null)
                return ResponseEntity.badRequest().body(ApiResponse.error("Current and new password are required"));
            if (newPassword.length() < 6)
                return ResponseEntity.badRequest().body(ApiResponse.error("New password must be at least 6 characters"));
            String result = riderService.updatePassword(rider.getEmail(), currentPassword, newPassword);
            return switch (result) {
                case "updated"          -> ResponseEntity.ok(ApiResponse.success("Password updated successfully"));
                case "wrong_password"   -> ResponseEntity.badRequest().body(ApiResponse.error("Current password is incorrect"));
                case "password_too_short" -> ResponseEntity.badRequest().body(ApiResponse.error("Password too short"));
                default                 -> ResponseEntity.badRequest().body(ApiResponse.error(result));
            };
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Change Email ──
    @PostMapping("/api/riders/{id}/change-email")
    public ResponseEntity<ApiResponse> changeEmailRest(@PathVariable String id, @RequestBody Map<String,String> body) {
        try {
            Rider rider = riderService.getRiderById(id);
            if (rider == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Rider not found"));
            String newEmail = body.get("newEmail");
            String password = body.get("password");
            if (newEmail == null || newEmail.trim().isEmpty())
                return ResponseEntity.badRequest().body(ApiResponse.error("New email is required"));
            if (password == null || password.trim().isEmpty())
                return ResponseEntity.badRequest().body(ApiResponse.error("Password confirmation required"));
            if (!newEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid email format"));
            Rider auth = riderService.loginRider(rider.getEmail(), password);
            if (auth == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Incorrect password"));
            String result = riderService.updateEmail(id, newEmail);
            return switch (result) {
                case "updated"     -> ResponseEntity.ok(ApiResponse.success("Email updated successfully"));
                case "email_taken" -> ResponseEntity.badRequest().body(ApiResponse.error("Email already in use"));
                default            -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Rider not found"));
            };
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Online Toggle ──
    @PostMapping("/api/riders/{id}/toggle-online")
    public ResponseEntity<ApiResponse> toggleOnline(@PathVariable String id, @RequestBody Map<String,Object> body) {
        try {
            boolean wantsOnline = Boolean.parseBoolean(String.valueOf(body.getOrDefault("online","false")));
            String result = riderService.toggleOnline(id, wantsOnline);
            return switch (result) {
                case "ok"                -> ResponseEntity.ok(ApiResponse.success(wantsOnline ? "You are now Online" : "You are now Offline"));
                case "missing_vehicle"   -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Please upload vehicle details before going online"));
                case "missing_documents" -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Please upload required documents before going online"));
                case "not_approved"      -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Your account is pending admin approval. You cannot go online yet."));
                default                  -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Rider not found"));
            };
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Vehicle Details Upload ──
    @PostMapping("/api/riders/{id}/vehicle")
    public ResponseEntity<ApiResponse> uploadVehicleDetails(
            @PathVariable String id,
            @RequestParam("bikeModel") String bikeModel,
            @RequestParam("bikeNumber") String bikeNumber,
            @RequestParam("vehicleRegistration") MultipartFile vehicleRegistration,
            @RequestParam("insuranceDocument") MultipartFile insuranceDocument,
            @RequestParam("vehiclePhoto") MultipartFile vehiclePhoto) {
        try {
            String result = riderService.saveVehicleDetails(id, bikeModel, bikeNumber,
                    vehicleRegistration, insuranceDocument, vehiclePhoto);
            return switch (result) {
                case "success"              -> ResponseEntity.ok(ApiResponse.success("Vehicle details uploaded successfully. Awaiting admin approval."));
                case "not_found"            -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Rider not found"));
                case "bike_model_required"  -> ResponseEntity.badRequest().body(ApiResponse.error("Bike model is required"));
                case "bike_number_required" -> ResponseEntity.badRequest().body(ApiResponse.error("Bike number is required"));
                case "registration_required"-> ResponseEntity.badRequest().body(ApiResponse.error("Vehicle registration document is required"));
                case "insurance_required"   -> ResponseEntity.badRequest().body(ApiResponse.error("Insurance document is required"));
                case "photo_required"       -> ResponseEntity.badRequest().body(ApiResponse.error("Vehicle photo is required"));
                default                     -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(result));
            };
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }

    // ── Rider Documents Upload ──
    @PostMapping("/api/riders/{id}/documents")
    public ResponseEntity<ApiResponse> uploadDocuments(
            @PathVariable String id,
            @RequestParam("nicDocument") MultipartFile nic,
            @RequestParam("drivingLicense") MultipartFile license,
            @RequestParam("riderPhoto") MultipartFile photo,
            @RequestParam("riderInsurance") MultipartFile insurance) {
        try {
            String result = riderService.saveRiderDocuments(id, nic, license, photo, insurance);
            return switch (result) {
                case "success"             -> ResponseEntity.ok(ApiResponse.success("Documents uploaded successfully. Awaiting admin approval."));
                case "not_found"           -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Rider not found"));
                case "nic_required"        -> ResponseEntity.badRequest().body(ApiResponse.error("NIC/ID card document is required"));
                case "license_required"    -> ResponseEntity.badRequest().body(ApiResponse.error("Driving license is required"));
                case "photo_required"      -> ResponseEntity.badRequest().body(ApiResponse.error("Rider photo is required"));
                case "insurance_required"  -> ResponseEntity.badRequest().body(ApiResponse.error("Insurance document is required"));
                default                    -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(result));
            };
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }

    // ── Admin: Approve ──
    @PostMapping("/api/riders/{id}/approve")
    public ResponseEntity<ApiResponse> approveRider(@PathVariable String id) {
        try {
            String result = riderService.approveRider(id);
            if ("approved".equals(result)) return ResponseEntity.ok(ApiResponse.success("Rider approved successfully"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Rider not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Admin: Reject ──
    @PostMapping("/api/riders/{id}/reject")
    public ResponseEntity<ApiResponse> rejectRider(@PathVariable String id, @RequestBody Map<String,String> body) {
        try {
            String reason = body.getOrDefault("reason", "Documents not acceptable");
            String result = riderService.rejectRider(id, reason);
            if ("rejected".equals(result)) return ResponseEntity.ok(ApiResponse.success("Rider rejected"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Rider not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── Legacy form-based endpoints (backward compat) ──
    @PostMapping("/rider/register")
    public String register(@RequestParam String name, @RequestParam String email,
                           @RequestParam String password, @RequestParam String phone) throws Exception {
        Rider rider = new Rider(); rider.setName(name); rider.setEmail(email);
        rider.setPassword(password); rider.setPhone(phone);
        return riderService.registerRider(rider);
    }

    @PostMapping("/rider/login")
    public String login(@RequestParam String email, @RequestParam String password) throws Exception {
        Rider rider = riderService.loginRider(email, password);
        if (rider != null) return "success:"+rider.getName()+":"+rider.getId()+":"+rider.getPhone();
        return "fail";
    }

    @PostMapping("/rider/update")
    public String update(@RequestParam String email, @RequestParam String name, @RequestParam String phone) throws Exception {
        return riderService.updateRider(email, name, phone) ? "updated" : "not found";
    }

    @PostMapping("/rider/delete")
    public String delete(@RequestParam String email) throws Exception {
        return riderService.deleteRider(email) ? "deleted" : "not found";
    }

    @GetMapping("/rider/all")
    public List<Rider> getAll() throws Exception { return riderService.getAllRiders(); }

    @PostMapping("/rider/updatePassword")
    public String updatePassword(@RequestParam String email, @RequestParam String currentPassword,
                                 @RequestParam String newPassword) throws Exception {
        return riderService.updatePassword(email, currentPassword, newPassword);
    }
}
