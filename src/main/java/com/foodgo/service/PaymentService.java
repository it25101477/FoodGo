package com.foodgo.service;

import com.foodgo.model.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.*;

@Service
public class PaymentService {

    private static final String FILE_NAME = "payments.txt";

    @Autowired
    private FileStorageService fileStorage;

    private String filePath() { return fileStorage.resolve(FILE_NAME); }

    @PostConstruct
    public void init() throws IOException {
        File file = new File(filePath());
        if (!file.exists()) { file.getParentFile().mkdirs(); file.createNewFile(); }
    }

    public Payment createPayment(Payment payment) throws IOException {
        if (payment.getId() == null || payment.getId().isEmpty())
            payment.setId(String.valueOf(System.currentTimeMillis()));
        if (payment.getCreatedAt() == 0) payment.setCreatedAt(System.currentTimeMillis());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath(), true))) {
            writer.write(payment.toFileLine()); writer.newLine();
        }
        return payment;
    }

    public boolean updateStatus(String paymentId, String newStatus) throws IOException {
        List<Payment> payments = getAllPayments();
        for (Payment p : payments) {
            if (p.getId().equals(paymentId)) {
                p.setStatus(newStatus);
                saveAll(payments); return true;
            }
        }
        return false;
    }

    public Payment getByOrderId(String orderId) throws IOException {
        for (Payment p : getAllPayments())
            if (p.getOrderId().equals(orderId)) return p;
        return null;
    }

    public List<Payment> getByUserId(String userId) throws IOException {
        List<Payment> result = new ArrayList<>();
        for (Payment p : getAllPayments())
            if (p.getUserId().equals(userId)) result.add(p);
        result.sort((a,b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        return result;
    }

    public List<Payment> getAllPayments() throws IOException {
        List<Payment> payments = new ArrayList<>();
        File file = new File(filePath());
        if (!file.exists()) return payments;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null)
                if (!line.trim().isEmpty()) payments.add(Payment.fromFileLine(line));
        }
        payments.sort((a,b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        return payments;
    }

    public boolean deletePayment(String paymentId) throws IOException {
        List<Payment> payments = getAllPayments();
        boolean removed = payments.removeIf(p -> p.getId().equals(paymentId));
        if (removed) saveAll(payments);
        return removed;
    }

    private void saveAll(List<Payment> payments) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath(), false))) {
            for (Payment p : payments) { writer.write(p.toFileLine()); writer.newLine(); }
        }
    }
}
