package com.foodgo.service;

import com.foodgo.model.Rider;
import com.foodgo.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class RiderService {

    private static final String FILE_NAME = "riders.txt";

    @Autowired
    private FileStorageService fileStorage;

    @Autowired
    private PasswordUtil passwordUtil;

    private String filePath() {
        return fileStorage.resolve(FILE_NAME);
    }

    @PostConstruct
    public void init() throws IOException {
        File file = new File(filePath());
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        // Ensure uploads sub-directory exists
        Path uploads = Paths.get(fileStorage.resolve("rider-uploads"));
        Files.createDirectories(uploads);
    }

    public String registerRider(Rider rider) throws IOException {
        if (findByEmail(rider.getEmail()) != null) return "Email already registered!";
        rider.setId(String.valueOf(System.currentTimeMillis()));
        rider.setPassword(passwordUtil.hash(rider.getPassword()));
        rider.setApprovalStatus("pending");
        rider.setCreatedAt(System.currentTimeMillis());
        rider.setUpdatedAt(System.currentTimeMillis());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath(), true))) {
            writer.write(rider.toFileLine());
            writer.newLine();
        }
        return "success";
    }

    public Rider loginRider(String email, String password) throws IOException {
        Rider rider = findByEmail(email);
        if (rider != null && passwordUtil.matches(password, rider.getPassword())) return rider;
        return null;
    }

    public Rider findByEmail(String email) throws IOException {
        for (Rider r : getAllRiders())
            if (r.getEmail().equalsIgnoreCase(email)) return r;
        return null;
    }

    public Rider getRiderById(String id) throws IOException {
        for (Rider r : getAllRiders())
            if (r.getId().equals(id)) return r;
        return null;
    }

    public List<Rider> getAllRiders() throws IOException {
        List<Rider> riders = new ArrayList<>();
        File file = new File(filePath());
        if (!file.exists()) return riders;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null)
                if (!line.trim().isEmpty()) riders.add(Rider.fromFileLine(line));
        }
        return riders;
    }

    public boolean updateRider(String email, String newName, String newPhone) throws IOException {
        List<Rider> riders = getAllRiders();
        for (Rider r : riders) {
            if (r.getEmail().equalsIgnoreCase(email)) {
                r.setName(newName); r.setPhone(newPhone);
                r.setUpdatedAt(System.currentTimeMillis());
                saveAll(riders); return true;
            }
        }
        return false;
    }

    public boolean updateRiderById(String id, String newName, String newPhone) throws IOException {
        List<Rider> riders = getAllRiders();
        for (Rider r : riders) {
            if (r.getId().equals(id)) {
                r.setName(newName); r.setPhone(newPhone);
                r.setUpdatedAt(System.currentTimeMillis());
                saveAll(riders); return true;
            }
        }
        return false;
    }

    public boolean deleteRider(String email) throws IOException {
        List<Rider> riders = getAllRiders();
        boolean removed = riders.removeIf(r -> r.getEmail().equalsIgnoreCase(email));
        if (removed) saveAll(riders);
        return removed;
    }

    public boolean deleteRiderById(String id) throws IOException {
        List<Rider> riders = getAllRiders();
        boolean removed = riders.removeIf(r -> r.getId().equals(id));
        if (removed) saveAll(riders);
        return removed;
    }

    public String updatePassword(String email, String currentPassword, String newPassword) throws IOException {
        if (newPassword == null || newPassword.length() < 6) return "password_too_short";
        List<Rider> riders = getAllRiders();
        for (Rider r : riders) {
            if (r.getEmail().equalsIgnoreCase(email)) {
                if (!passwordUtil.matches(currentPassword, r.getPassword())) return "wrong_password";
                r.setPassword(passwordUtil.hash(newPassword));
                r.setUpdatedAt(System.currentTimeMillis());
                saveAll(riders); return "updated";
            }
        }
        return "not_found";
    }

    public String updateEmail(String id, String newEmail) throws IOException {
        Rider existing = findByEmail(newEmail);
        if (existing != null && !existing.getId().equals(id)) return "email_taken";
        List<Rider> riders = getAllRiders();
        for (Rider r : riders) {
            if (r.getId().equals(id)) {
                r.setEmail(newEmail.trim().toLowerCase());
                r.setUpdatedAt(System.currentTimeMillis());
                saveAll(riders); return "updated";
            }
        }
        return "not_found";
    }

    // ── Online Status ──
    public String toggleOnline(String id, boolean wantsOnline) throws IOException {
        Rider rider = getRiderById(id);
        if (rider == null) return "not_found";
        if (wantsOnline && !rider.canGoOnline()) {
            if (!rider.isVehicleDetailsSubmitted()) return "missing_vehicle";
            if (!rider.isDocumentsSubmitted()) return "missing_documents";
            return "not_approved";
        }
        List<Rider> riders = getAllRiders();
        for (Rider r : riders) {
            if (r.getId().equals(id)) {
                r.setOnline(wantsOnline);
                r.setUpdatedAt(System.currentTimeMillis());
                saveAll(riders); return "ok";
            }
        }
        return "not_found";
    }

    // ── Vehicle Details ──
    public String saveVehicleDetails(String riderId, String bikeModel, String bikeNumber,
                                     MultipartFile regDoc, MultipartFile insurDoc, MultipartFile photo) throws IOException {
        Rider rider = getRiderById(riderId);
        if (rider == null) return "not_found";

        String uploadDir = fileStorage.resolve("rider-uploads");
        if (bikeModel == null || bikeModel.trim().isEmpty()) return "bike_model_required";
        if (bikeNumber == null || bikeNumber.trim().isEmpty()) return "bike_number_required";
        if (regDoc == null || regDoc.isEmpty()) return "registration_required";
        if (insurDoc == null || insurDoc.isEmpty()) return "insurance_required";
        if (photo == null || photo.isEmpty()) return "photo_required";

        String regPath   = saveFile(uploadDir, riderId+"_vreg_", regDoc);
        String insurPath = saveFile(uploadDir, riderId+"_vins_", insurDoc);
        String photoPath = saveFile(uploadDir, riderId+"_vpho_", photo);

        List<Rider> riders = getAllRiders();
        for (Rider r : riders) {
            if (r.getId().equals(riderId)) {
                r.setBikeModel(bikeModel.trim());
                r.setBikeNumber(bikeNumber.trim());
                r.setVehicleRegistration(regPath);
                r.setInsuranceDocument(insurPath);
                r.setVehiclePhoto(photoPath);
                r.setVehicleDetailsSubmitted(true);
                r.setApprovalStatus("pending");
                r.setUpdatedAt(System.currentTimeMillis());
                saveAll(riders); return "success";
            }
        }
        return "not_found";
    }

    // ── Rider Documents ──
    public String saveRiderDocuments(String riderId, MultipartFile nic, MultipartFile license,
                                     MultipartFile photo, MultipartFile insurance) throws IOException {
        Rider rider = getRiderById(riderId);
        if (rider == null) return "not_found";

        String uploadDir = fileStorage.resolve("rider-uploads");
        if (nic == null || nic.isEmpty()) return "nic_required";
        if (license == null || license.isEmpty()) return "license_required";
        if (photo == null || photo.isEmpty()) return "photo_required";
        if (insurance == null || insurance.isEmpty()) return "insurance_required";

        String nicPath   = saveFile(uploadDir, riderId+"_nic_",  nic);
        String licPath   = saveFile(uploadDir, riderId+"_lic_",  license);
        String phoPath   = saveFile(uploadDir, riderId+"_pho_",  photo);
        String insPath   = saveFile(uploadDir, riderId+"_ins_",  insurance);

        List<Rider> riders = getAllRiders();
        for (Rider r : riders) {
            if (r.getId().equals(riderId)) {
                r.setNicDocument(nicPath);
                r.setDrivingLicense(licPath);
                r.setRiderPhoto(phoPath);
                r.setRiderInsurance(insPath);
                r.setDocumentsSubmitted(true);
                r.setApprovalStatus("pending");
                r.setUpdatedAt(System.currentTimeMillis());
                saveAll(riders); return "success";
            }
        }
        return "not_found";
    }

    // ── Admin Approval ──
    public String approveRider(String riderId) throws IOException {
        List<Rider> riders = getAllRiders();
        for (Rider r : riders) {
            if (r.getId().equals(riderId)) {
                r.setApprovalStatus("approved");
                r.setRejectionReason("");
                r.setUpdatedAt(System.currentTimeMillis());
                saveAll(riders); return "approved";
            }
        }
        return "not_found";
    }

    public String rejectRider(String riderId, String reason) throws IOException {
        List<Rider> riders = getAllRiders();
        for (Rider r : riders) {
            if (r.getId().equals(riderId)) {
                r.setApprovalStatus("rejected");
                r.setRejectionReason(reason != null ? reason : "Documents not acceptable");
                r.setOnline(false);
                r.setUpdatedAt(System.currentTimeMillis());
                saveAll(riders); return "rejected";
            }
        }
        return "not_found";
    }

    public List<Rider> getPendingRiders() throws IOException {
        List<Rider> result = new ArrayList<>();
        for (Rider r : getAllRiders())
            if ("pending".equals(r.getApprovalStatus()) && (r.isVehicleDetailsSubmitted() || r.isDocumentsSubmitted()))
                result.add(r);
        return result;
    }

    // ── Helpers ──
    private String saveFile(String dir, String prefix, MultipartFile file) throws IOException {
        String origName = file.getOriginalFilename();
        String ext = (origName != null && origName.contains(".")) ? origName.substring(origName.lastIndexOf('.')) : ".bin";
        String filename = prefix + System.currentTimeMillis() + ext;
        Path dest = Paths.get(dir, filename);
        Files.createDirectories(dest.getParent());
        file.transferTo(dest.toFile());
        return "rider-uploads/" + filename;
    }

    private void saveAll(List<Rider> riders) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath(), false))) {
            for (Rider r : riders) {
                writer.write(r.toFileLine());
                writer.newLine();
            }
        }
    }
}
