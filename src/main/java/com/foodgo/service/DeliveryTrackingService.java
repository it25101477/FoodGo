package com.foodgo.service;

import com.foodgo.model.DeliveryTracking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.*;

@Service
public class DeliveryTrackingService {

    private static final String FILE_NAME = "tracking.txt";

    @Autowired
    private FileStorageService fileStorage;

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
    }

    public String createTracking(DeliveryTracking tracking) throws IOException {
        if (tracking.getTrackingId() == null || tracking.getTrackingId().isEmpty()) {
            tracking.setTrackingId(String.valueOf(System.currentTimeMillis()));
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath(), true))) {
            writer.write(tracking.toFileLine());
            writer.newLine();
        }
        return tracking.getTrackingId();
    }

    public List<DeliveryTracking> getAllTrackings() throws IOException {
        List<DeliveryTracking> trackings = new ArrayList<>();
        File file = new File(filePath());
        if (!file.exists()) return trackings;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    trackings.add(DeliveryTracking.fromFileLine(line));
                }
            }
        }
        return trackings;
    }

    public DeliveryTracking getTrackingByOrderId(String orderId) throws IOException {
        File file = new File(filePath());
        if (!file.exists()) return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    DeliveryTracking t = DeliveryTracking.fromFileLine(line);
                    if (t.getOrderId().equals(orderId)) {
                        return t;
                    }
                }
            }
        }
        return null;
    }

    public List<DeliveryTracking> getTrackingByRiderId(String riderId) throws IOException {
        List<DeliveryTracking> trackings = new ArrayList<>();
        File file = new File(filePath());
        if (!file.exists()) return trackings;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    DeliveryTracking t = DeliveryTracking.fromFileLine(line);
                    if (t.getRiderId().equals(riderId)) {
                        trackings.add(t);
                    }
                }
            }
        }
        return trackings;
    }

    public boolean updateTrackingLocation(String trackingId, double latitude, double longitude) throws IOException {
        return updateTracking(trackingId, t -> {
            t.setCurrentLatitude(latitude);
            t.setCurrentLongitude(longitude);
        });
    }

    public boolean updateTrackingStatus(String trackingId, String newStatus) throws IOException {
        return updateTracking(trackingId, t -> {
            t.setStatus(newStatus);
            if ("delivered".equalsIgnoreCase(newStatus)) {
                t.setDeliveredAt(System.currentTimeMillis());
            }
        });
    }

    private boolean updateTracking(String trackingId, java.util.function.Consumer<DeliveryTracking> updater) throws IOException {
        List<DeliveryTracking> trackings = getAllTrackings();
        boolean found = false;
        for (DeliveryTracking t : trackings) {
            if (t.getTrackingId().equals(trackingId)) {
                updater.accept(t);
                found = true;
                break;
            }
        }
        if (found) saveAll(trackings);
        return found;
    }

    private void saveAll(List<DeliveryTracking> trackings) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath(), false))) {
            for (DeliveryTracking t : trackings) {
                writer.write(t.toFileLine());
                writer.newLine();
            }
        }
    }
}
